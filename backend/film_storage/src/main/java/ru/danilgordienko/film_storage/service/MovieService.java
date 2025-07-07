package ru.danilgordienko.film_storage.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListCacheDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListDto;
import ru.danilgordienko.film_storage.DTO.PageDto;
import ru.danilgordienko.film_storage.DTO.mapping.MovieMapping;
import ru.danilgordienko.film_storage.MovieAPI.MovieApiClient;
import ru.danilgordienko.film_storage.config.RabbitConfig;
import ru.danilgordienko.film_storage.model.*;
import ru.danilgordienko.film_storage.repository.GenreRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.MovieSearchRepository;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableCaching
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapping  movieMapping;
    private final MovieSearchRepository movieSearchRepository;
    private final RabbitTemplate rabbitTemplate;
    private final int size = 20;

    // Получение всех фильмов из базы данных
    public List<MovieListDto> getAllMovies(){
        log.info("Получение всех фильмов из базы данных");
        var movies = movieRepository.findAll().stream()
                .map(movieMapping::toMovieListDto)
                .toList();
        log.info("Найдено {} фильмов", movies.size());
        return movies;
    }

    // Получение всех фильмов из базы данных
    @Cacheable(value = "movies", key = "#page", condition = "#page == 0")
    public PageDto getMoviesPage(int page){
        log.info("Получение всех фильмов из базы данных");
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<Movie> moviePage = movieRepository.findAll(pageable);

        // Преобразуем содержимое страницы
        List<MovieListCacheDto> dtoList = moviePage.getContent()
                .stream()
                .map(movieMapping::toMovieListCacheDto)
                .toList();

        // Возвращаем новую страницу с теми же параметрами
        Page<MovieListCacheDto> dtoPage = new PageImpl<>(dtoList, pageable, moviePage.getTotalElements());
        log.info("Найдено {} фильмов", dtoList.size());
        return movieMapping.toPageDto(dtoPage);
    }

    // Получение всех жанров из базы данных
    private List<Genre> getAllGenres(){
        log.info("Получение всех жанров из базы данных");
        var genres = genreRepository.findAll();
        log.info("Найдено {} фильмов", genres.size());
        return genres;
    }

    // Получение фильма по ID
    public Optional<MovieDetailsDto> getMovie(Long id) {
        log.info("Получение фильма с ID = {}", id);
        return movieRepository.findById(id)
                .map(movie -> {
                    log.info("Фильм найден: {}", movie.getTitle());
                    return movieMapping.toMovieDetailsDto(movie);
                });
    }

    //Поиск фильмов по запросу query
    public List<MovieListDto> searchMoviesByTitle(String query) {
        log.info("Поиск фильмов в Elasticsearch по названию: {}", query);

        var searchResults = movieSearchRepository.searchByTitle(query);

        var movies = searchResults.stream()
                .map(movieMapping::toMovieListDto)
                .toList();

        log.info("Найдено {} фильмов в Elasticsearch по запросу '{}'", movies.size(), query);

        return movies;
    }

    //Поиск фильмов по запросу query
    public Page<MovieListDto> searchMoviesPageByTitle(String query,  int page) {
        log.info("Поиск фильмов в Elasticsearch по названию: {}", query);
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        var searchResults = movieSearchRepository.findByTitleContaining(query, pageable);

        // Преобразуем содержимое страницы
        List<MovieListDto> dtoList = searchResults.getContent()
                .stream()
                .map(movieMapping::toMovieListDto)
                .toList();

        // Возвращаем новую страницу с теми же параметрами
        Page<MovieListDto> dtoPage = new PageImpl<>(dtoList, pageable, searchResults.getTotalElements());
        log.info("Найдено {} фильмов", dtoList.size());
        return dtoPage;
    }

    // получение постера к фильму(отпправляет запрос брокеру и ждет ответа)
    @Cacheable(value = "movies", key = "#id", cacheManager = "binaryCacheManager")
    public byte[] getPoster(Long id) {
        return movieRepository.findById(id)
        .map(movie -> {
            log.info("Фильм с id: {} найден, запрос постера", id);
            return (byte[]) rabbitTemplate.convertSendAndReceive(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_POSTER,
                    movie.getPoster()
            );
        })
        .orElse(new byte[0]);
    }

    // приклпляет уже существующий жанр к фильму
    private void attachGenresToMovies(List<Movie> movies) {
        Map<Long, Genre> genresMap = getAllGenres().stream()
                .collect(Collectors.toMap(Genre::getTmdbId, Function.identity()));

        for (Movie movie : movies) {
            Set<Genre> attachedGenres = movie.getGenres().stream()
                    .map(genre -> genresMap.get(genre.getTmdbId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            movie.setGenres(attachedGenres);
        }
    }

    private Object getRabbitResponse(String exchange, String routingKey, Object body) {
        log.info("Запрос к RabbitMQ с телом: {}", body.toString());
        try {
            return rabbitTemplate.convertSendAndReceive(
                    exchange,
                    routingKey,
                    body
            );
        } catch (AmqpException e) {
            log.error("Ошибка при работе с RabbitMQ: {}", e.getMessage(), e);
            return null;
        } catch (ClassCastException e) {
            log.error("Ошибка преобразования ответа: {}", e.getMessage(), e);
            return null;
        }
    }

    // заполнение бд фильмами по страницам(пока недоделан)
    @Transactional
    public boolean getPopularMovies() {

        int page = 1;
        Object response = getRabbitResponse(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY_PAGE,
                page
        );

        if (response == null) {
            log.warn("Ответ от сервиса фильмов не получен");
            return false;
        }

        List<MovieDto> movies;
        try {
            movies = (List<MovieDto>) response;
        } catch (ClassCastException e) {
            log.error("Ошибка приведения типов: {}", e.getMessage());
            return false;
        }

        log.info("Получено {} фильмов", movies.size());
        saveReceivedMovies(movies);
        return true;
    }

    @Transactional
    protected void saveReceivedMovies(List<MovieDto> movies) {
        var mappedMovies = movies.stream().map(movieMapping::toMovie).toList();
        // заменяем пришедшие жанры на уже созданые в бд
        attachGenresToMovies(mappedMovies);
        // сохраняем в бд
        saveMoviesDB(mappedMovies);
        // сохраняем в elasticsearch
        saveMoviesES(mappedMovies);

        log.info("Сохранено {} фильмов", movies.size());
    }

    // Заполнение базы данных недавно вышедшими фильмами
    @Transactional
    @RabbitListener(queues = "movies.queue")
    public void populateMovies(List<MovieDto> movies) {
        log.info("Получение недавно вышедших фильмов");
        saveReceivedMovies(movies);
    }

    // сохранение фильмов в бд
    private void saveMoviesDB(List<Movie> movies){
        log.info("Сохранение фильмов в бд");
        List<Movie> savedMovies = movieRepository.saveAll(movies);

        //если не получилось добавить в бд, выбрасываем исключение, чтоб транзация откатилась
        if (savedMovies.size() != movies.size()) {
            throw new IllegalStateException("Не все фильмы были сохранены. Откат транзакции.");
        }
    }

    // сохранение фильмов в elasticsearchы
    private void saveMoviesES(List<Movie> movies){
        log.info("Сохранение фильмов в elasticsearch");
        var savedMovies = movieSearchRepository.saveAll(movies
                .stream()
                .map(movieMapping::toMovieDocument)
                .toList());

        long savedCount = StreamSupport.stream(savedMovies.spliterator(), false).count();
        //если не получилось добавить в elasticsearch, выбрасываем исключение, чтоб транзация откатилась
        if (savedCount != movies.size()) {
            throw new IllegalStateException("Не все фильмы были сохранены в Elasticsearch");
        }
    }

}

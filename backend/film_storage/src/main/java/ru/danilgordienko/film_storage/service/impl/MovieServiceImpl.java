package ru.danilgordienko.film_storage.service.impl;


import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListCacheDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListDto;
import ru.danilgordienko.film_storage.DTO.PageDto;
import ru.danilgordienko.film_storage.DTO.mapping.MovieMapping;
import ru.danilgordienko.film_storage.MovieAPI.MovieApiClient;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.ElasticsearchConnectionException;
import ru.danilgordienko.film_storage.exception.MovieNotFoundException;
import ru.danilgordienko.film_storage.exception.MovieSaveException;
import ru.danilgordienko.film_storage.model.*;
import ru.danilgordienko.film_storage.repository.GenreRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.MovieSearchRepository;
import ru.danilgordienko.film_storage.service.MovieService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableCaching
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapping  movieMapping;
    private final MovieSearchRepository movieSearchRepository;
    private final MovieApiClient movieApiClient;
    private final int size = 20;

    // Получение всех фильмов из базы данных
    public List<MovieListDto> getAllMovies(){
        try {
            var movies = movieRepository.findAll().stream()
                    .map(movieMapping::toMovieListDto)
                    .toList();
            log.info("Найдено {} фильмов", movies.size());
            return movies;
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    // Получение всех фильмов из базы данных
    @Cacheable(value = "movies", key = "#page", condition = "#page == 0")
    public PageDto getMoviesPage(int page){
        try {
            if (page < 0) {
                log.warn("Попытка получения страницы < 0: {}", page);
            }
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
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    // Получение всех жанров из базы данных
    private List<Genre> getAllGenres(){
        try {
            log.info("Получение всех жанров из базы данных");
            var genres = genreRepository.findAll();
            log.info("Найдено {} жанров", genres.size());
            return genres;
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    // Получение фильма по ID
    public MovieDetailsDto getMovie(Long id) {
            var movie = getMovieById(id);
            return movieMapping.toMovieDetailsDto(movie);
    }

    // Получение фильма по ID
    public Movie getMovieById(Long id) {
        try {
            log.info("Получение фильма с ID = {}", id);
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("");
                        return new MovieNotFoundException("Фильм с id " + id + " не найден");
                    });
            log.info("Фильм найден: {}", movie.getTitle());
            return movie;
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    //Поиск фильмов по запросу query
    public List<MovieListDto> searchMoviesByTitle(String query) {
        try {
            log.info("Поиск фильмов в Elasticsearch по названию: {}", query);

            if (query == null || query.isBlank()) {
                log.warn("Пустой или null запрос на поиск фильмов");
                return List.of();
            }
            var searchResults = movieSearchRepository.searchByTitle(query);

            var movies = searchResults.stream()
                    .map(movieMapping::toMovieListDto)
                    .toList();

            log.info("Найдено {} фильмов в Elasticsearch по запросу '{}'", movies.size(), query);

            return movies;
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Ошибка при работе с Elasticsearch", e);
            throw new ElasticsearchConnectionException("Не удалось найти пользователя в Elasticsearch", e);
        }
    }

    //Поиск фильмов по запросу query
    public PageDto searchMoviesPageByTitle(String query,  int page) {
        try {
            log.info("Поиск фильмов в Elasticsearch по названию: {}", query);
            Pageable pageable = PageRequest.of(page, size);
            var searchResults = movieSearchRepository.findByTitleContaining(query, pageable);

            // Преобразуем содержимое страницы
            List<MovieListDto> dtoList = searchResults.getContent()
                    .stream()
                    .map(movieMapping::toMovieListDto)
                    .toList();

            // Возвращаем новую страницу с теми же параметрами
            Page<MovieListDto> dtoPage = new PageImpl<>(dtoList, pageable, searchResults.getTotalElements());
            log.info("Найдено {} фильмов", dtoList.size());
            return movieMapping.toMovieListPageDto(dtoPage);
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Ошибка при работе с Elasticsearch", e);
            throw new ElasticsearchConnectionException("Не удалось найти пользователя в Elasticsearch", e);
        }
    }

    // получение постера к фильму(отпправляет запрос брокеру и ждет ответа)
    @Cacheable(value = "movies", key = "#id", cacheManager = "binaryCacheManager")
    public byte[] getPoster(Long id) {
        Movie movie = getMovieById(id);
        return movieApiClient.getPoster(movie);
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

    // заполнение бд фильмами по страницам(пока недоделан)
    @Transactional
    public boolean getPopularMovies() {

        int page = 1;
        var movies = movieApiClient.getPopularMoviesPage(page);

        log.info("Получено {} фильмов", movies.size());
        saveReceivedMovies(movies);
        return true;
    }

    // cохранение фильмов в elastic и бд
    private void saveReceivedMovies(List<MovieDto> movies) {
        try {
            var mappedMovies = movies.stream().map(movieMapping::toMovie).toList();
            // заменяем пришедшие жанры на уже созданые в бд
            attachGenresToMovies(mappedMovies);
            // сохраняем в бд
            saveMoviesDB(mappedMovies);
            // сохраняем в elasticsearch
            saveMoviesES(mappedMovies);

            log.info("Сохранено {} фильмов", movies.size());
            // пробрасываем все ошибки дальше, чтоб транзакция откатилась
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Ошибка при работе с Elasticsearch", e);
            throw new ElasticsearchConnectionException("Не удалось найти пользователя в Elasticsearch", e);
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        } catch (Exception e) {
            log.error("Ошибка при сохранении фильмов", e);
            throw new MovieSaveException("Ошибка при сохранении фильмов", e);
        }
    }

    // Заполнение базы данных недавно вышедшими фильмами
    @Override
    @Transactional
    @EventListener(MovieApiClient.MoviesReceivedEvent .class)
    public void populateMovies(MovieApiClient.MoviesReceivedEvent event) {
        log.info("Сохранение недавно вышедших фильмов");
        List<MovieDto> movies = event.getMovies();
        saveReceivedMovies(movies);
    }

    // удаление фильма(только для админов)
    @Override
    @Transactional
    public void deleteMovie(Long id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            movieSearchRepository.deleteById(id);
        }
    }

    @Override
    public void addMovie(Long id) {

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

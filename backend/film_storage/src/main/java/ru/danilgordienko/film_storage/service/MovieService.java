package ru.danilgordienko.film_storage.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListDto;
import ru.danilgordienko.film_storage.DTO.mapping.MovieMapping;
import ru.danilgordienko.film_storage.MovieAPI.MovieApiClient;
import ru.danilgordienko.film_storage.model.*;
import ru.danilgordienko.film_storage.repository.GenreRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.MovieSearchRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieApiClient movieApiClient;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapping  movieMapping;
    private final MovieSearchRepository movieSearchRepository;

//    @PostConstruct
//    public void init() {
//        populateMovies();
//        //var movies = movieRepository.findAll();
//        //movieSearchRepository.saveAll(movies.stream().map(movieMapping::toMovieDocument).collect(Collectors.toList()));
//    }

    // Получение всех фильмов из базы данных
    public List<MovieListDto> getAllMovies(){
        log.info("Получение всех фильмов из базы данных");
        var movies = movieRepository.findAll().stream()
                .map(movieMapping::toMovieListDto)
                .collect(Collectors.toList());
        log.info("Найдено {} фильмов", movies.size());
        return movies;
    }

    // Получение всех жанров из базы данных
    private List<Genre> getAllGenres(){
        log.info("Получение всех фильмов из базы данных");
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

    public byte[] getPoster(Long id) {
        return movieRepository.findById(id)
                .map(m -> {
                    log.info("Фильм с id: {} найден, запрос постера", id);
                    return movieApiClient.downloadPoster(m.getPoster());
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

    // Заполнение базы данных недавно вышедшими фильмами
    @Transactional
    //@RabbitListener(queues = "movies.queue")
    public void populateMovies(List<MovieDto> movies) {
        log.info("Получение недавно вышедших фильмов");
        //List<Movie> movies = movieApiClient.getRecentMovies();
        if (movies.isEmpty()) {
            log.warn("Фильмы не получены. Не удалось пополнить новые фильмы.");
            return;
        }
        // заменяем пришедшие жанры на уже созданые в бд
        var mappedMovies = movies.stream().map(movieMapping::toMovie).toList();
        attachGenresToMovies(mappedMovies);
        log.info("Сохранение фильмов в бд");
        List<Movie> savedMovies = movieRepository.saveAll(mappedMovies);

        //если не получилось добавить в бд, то в elastic не сохраняем
        if (savedMovies.size() != movies.size()) {
            throw new IllegalStateException("Не все фильмы были сохранены. Откат транзакции.");
        }

        log.info("Сохранение фильмов в elasticsearch");
        var savedMoviesEl = movieSearchRepository.saveAll(mappedMovies.stream().map(movieMapping::toMovieDocument).toList());

        log.info("Сохранено {} фильмов", movies.size());
    }

}

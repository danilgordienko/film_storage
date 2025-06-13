package ru.danilgordienko.film_storage.service;


import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MovieListDto;
import ru.danilgordienko.film_storage.DTO.mapping.MovieMapping;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.TmdbAPI.TmdbClient;
import ru.danilgordienko.film_storage.model.*;
import ru.danilgordienko.film_storage.TmdbAPI.TmdbMovie;
import ru.danilgordienko.film_storage.repository.GenreRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.MovieSearchRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final TmdbClient tmdbClient;

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapping  movieMapping;
    private final MovieSearchRepository movieSearchRepository;

//    @PostConstruct
//    public void init() {
//        //populateMovies();
//        var movies = movieRepository.findAll();
//        movieSearchRepository.saveAll(movies.stream().map(movieMapping::toMovieDocument).collect(Collectors.toList()));
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
                .map(Movie::getPoster)
                .orElse(new byte[0]);

    }

    //получение списка фильмов из внешнего api
    public List<Movie> getPopularMovies() {
        log.info("Загрузка популярных фильмов из внешнего API...");
        List<TmdbMovie> tmdbMovies = tmdbClient.getPopularMovies();

        if (tmdbMovies == null || tmdbMovies.isEmpty()) {
            log.warn("Не удалось получить популярные фильмы или список пуст");
            return List.of();
        }
        log.info("Получено {} фильмов с внешнего API", tmdbMovies.size());

        // Привязка жанров по ID
        tmdbMovies = tmdbMovies.stream().peek(movie -> {
            Set<Genre> genres = Optional.ofNullable(movie.getGenreIds())
                    .orElse(List.of()).stream()
                    .map(genreRepository::findByTmdbId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            movie.setGenres(genres);
        }).toList();

        // Преобразование в модель Movie
        List<Movie> movies = new ArrayList<>();
        for (var tmdbMovie : tmdbMovies) {
            var movie = convertToMovie(tmdbMovie);
            movies.add(movie);
            log.debug("Преобразован фильм: {}", movie.getTitle());
        }

        return movies;
    }


    // Преобразуем данные TMDb в модель Movie
    public Movie convertToMovie(TmdbMovie tmdbMovie) {
        Movie movie = new Movie();
        movie.setTitle(tmdbMovie.getTitle());
        movie.setDescription(tmdbMovie.getOverview());
        // Преобразуем строку "yyyy-MM-dd" в java.util.Date
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date releaseDate = dateFormat.parse(tmdbMovie.getReleaseDate());
            movie.setRelease_date(releaseDate);
        } catch (ParseException | NullPointerException e) {
            log.warn("Ошибка парсинга даты фильма '{}': {}", tmdbMovie.getTitle(), tmdbMovie.getReleaseDate());
            movie.setRelease_date(null);
        }

        movie.setGenres(tmdbMovie.getGenres());

        // Загружаем постер
//        if (tmdbMovie.getPosterPath() != null) {
//            byte[] poster = tmdbClient.downloadPoster(tmdbMovie.getPosterPath());
//            movie.setPoster(poster);
//        }

        return movie;
    }

    // Заполнение базы данных фильмами и жанрами
    @Transactional
    public void populateMovies() {
        log.info("Заполнение базы данных жанрами и популярными фильмами...");
        List<Genre> genres = tmdbClient.loadGenres();
        genreRepository.saveAll(genres);
        log.info("Сохранено {} жанров", genres.size());

        List<Movie> movies = getPopularMovies();
        // Проверяем каждый фильм перед сохранением
//        movies.forEach(movie -> {
//            if (movie.getPoster() == null || movie.getPoster().length == 0) {
//                log.warn("Фильм '{}' не имеет постера, сохраняем как пустой.", movie.getTitle());
//                movie.setPoster(new byte[0]);  // Убедись, что всегда есть массив байт
//            }
//        });
        movieRepository.saveAll(movies);
        movieSearchRepository.saveAll(movies.stream().map(movieMapping::toMovieDocument).toList());
        log.info("Сохранено {} фильмов", movies.size());
    }

}

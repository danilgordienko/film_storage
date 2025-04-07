package ru.danilgordienko.film_storage.service;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.TmdbAPI.TmdbClient;
import ru.danilgordienko.film_storage.model.Genre;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.TmdbAPI.TmdbMovie;
import ru.danilgordienko.film_storage.repository.GenreRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final TmdbClient tmdbClient;

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

//    @PostConstruct
//    public void init() {
//        tmdbClient.loadGenres();
//        populateMovies();
//    }

    public List<Movie> getAllMovies(){
        return movieRepository.findAll();
    }

    public List<Movie> getPopularMovies() {
        // Загружаем фильмы
        List<TmdbMovie> tmdbMovies = tmdbClient.getPopularMovies();

        if (tmdbMovies == null || tmdbMovies.isEmpty()) {
            return List.of();
        }

        //Получаем названия жанров по TmdbId
        tmdbMovies = tmdbMovies.stream().peek(movie -> {
            Set<Genre> genres = Optional.ofNullable(movie.getGenreIds())
                    .orElse(List.of()).stream()
                    .map(genreRepository::findByTmdbId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            movie.setGenres(genres);
        }).toList();

        //Преобразуем все фильмы в нужную модель
        List<Movie> movies = new ArrayList<>();
        for (var movie: tmdbMovies){
            movies.add(convertToMovie(movie));
        }
        return movies;
    }


    // Преобразуем данные TMDb в модель Movie
    private Movie convertToMovie(TmdbMovie tmdbMovie) {
        Movie movie = new Movie();
        movie.setTitle(tmdbMovie.getTitle());
        movie.setDescription(tmdbMovie.getOverview());
        // Преобразуем строку "yyyy-MM-dd" в java.util.Date
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date releaseDate = dateFormat.parse(tmdbMovie.getReleaseDate());
            movie.setRelease_date(releaseDate);
        } catch (ParseException e) {
            movie.setRelease_date(null);
        }

        movie.setGenres(tmdbMovie.getGenres());
        return movie;
    }

    // Заполнение базы данных фильмами и жанрами
    public void populateMovies() {
        List<Genre> genres = tmdbClient.loadGenres();
        genreRepository.saveAll(genres);
        List<Movie> movies = getPopularMovies();
        movieRepository.saveAll(movies);
    }

}

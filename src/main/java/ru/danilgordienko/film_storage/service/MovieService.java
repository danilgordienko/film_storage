package ru.danilgordienko.film_storage.service;


import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.danilgordienko.film_storage.DTO.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MovieListDto;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.DTO.mapping.MovieMapping;
import ru.danilgordienko.film_storage.TmdbAPI.TmdbClient;
import ru.danilgordienko.film_storage.model.Genre;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.TmdbAPI.TmdbMovie;
import ru.danilgordienko.film_storage.model.Rating;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.GenreRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.RatingRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;

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
    private final MovieMapping  movieMapping;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

//    @PostConstruct
//    public void init() {
//        populateMovies();
//    }

    public List<MovieListDto> getAllMovies(){
        return movieRepository.findAll().stream().map(movieMapping::toMovieListDto).collect(Collectors.toList());
    }

    public Optional<MovieDetailsDto> getMovie(Long id){
        return movieRepository.findById(id).map(movieMapping::toMovieDetailsDto);
    }

    //добавляем рейтинг к фильму
    public void addRating(Long id, RatingDto rating) {
        //забираем username из текущей аунтификации
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм не найден"));

        if (rating.getRating() < 1 || rating.getRating() > 10) {
            throw new IllegalArgumentException("Рейтинг должен быть от 1 до 10");
        }

        Rating rate = Rating.builder()
                .movie(movie)
                .user(user)
                .rating(rating.getRating())
                .comment(rating.getComment())
                .build();

        ratingRepository.save(rate);
    }


    //получение списка фильмов
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

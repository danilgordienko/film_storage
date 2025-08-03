package ru.danilgordienko.film_storage.service;

import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListDto;
import ru.danilgordienko.film_storage.DTO.PageDto;
import ru.danilgordienko.film_storage.MovieAPI.MovieApiClient;
import ru.danilgordienko.film_storage.model.Movie;

import java.util.List;

@Service
public interface MovieService {
    List<MovieListDto> getAllMovies();
    PageDto getMoviesPage(int page);
    MovieDetailsDto getMovie(Long id);
    Movie getMovieById(Long id);
    List<MovieListDto> searchMoviesByTitle(String query);
    PageDto searchMoviesPageByTitle(String query, int page);
    byte[] getPoster(Long id);
    boolean getPopularMovies();
    void populateMovies(MovieApiClient.MoviesReceivedEvent event);
    void deleteMovie(Long id);
    void addMovie(Long id);
}

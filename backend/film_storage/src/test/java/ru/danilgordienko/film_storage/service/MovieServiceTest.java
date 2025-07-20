package ru.danilgordienko.film_storage.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.danilgordienko.film_storage.MovieAPI.MovieApiClient;
import ru.danilgordienko.film_storage.model.Genre;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.repository.GenreRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {
    @InjectMocks
    private MovieService movieService;

    @Mock
    private MovieApiClient movieApiClient;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private GenreRepository genreRepository;

//    @Test
//    public void testGetAllMovies() {
//        Movie movie = new Movie();
//        movie.setTitle("Test Movie");
//        when(movieRepository.findAll()).thenReturn(List.of(movie));
//
//        List<Movie> result = movieService.getAllMovies();
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("Test Movie", result.get(0).getTitle());
//    }

    @Test
    public void testGetPopularMovies() {
        TmdbMovie tmdbMovie = new TmdbMovie();
        tmdbMovie.setTitle("Test Movie");
        tmdbMovie.setGenreIds(List.of(1L));
        Genre genre = new Genre();
        genre.setTmdbId(1L);
        genre.setName("Action");
        when(movieApiClient.getPopularMovies()).thenReturn(List.of(tmdbMovie));
        when(genreRepository.findByTmdbId(1L)).thenReturn(Optional.of(genre));

        List<Movie> result = movieService.getPopularMovies();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.getFirst().getTitle());
        assertTrue(result.getFirst().getGenres().contains(genre));
    }

    @Test
    public void testGetPopularMoviesWhenEmpty() {
        when(movieApiClient.getPopularMovies()).thenReturn(List.of());

        List<Movie> result = movieService.getPopularMovies();

        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testConvertToMovie() throws ParseException {
        TmdbMovie tmdbMovie = new TmdbMovie();
        tmdbMovie.setTitle("Test Movie");
        tmdbMovie.setOverview("Description");
        tmdbMovie.setReleaseDate("2025-01-01");

        Movie movie = movieService.convertToMovie(tmdbMovie);

        assertNotNull(movie);
        assertEquals("Test Movie", movie.getTitle());
        assertEquals("Description", movie.getDescription());
        assertNotNull(movie.getRelease_date());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-01"), movie.getRelease_date());
    }

    @Test
    public void testConvertToMovieWithIncorrectData() {
        TmdbMovie tmdbMovie = new TmdbMovie();
        tmdbMovie.setTitle("Test Movie");
        tmdbMovie.setOverview("Description");
        tmdbMovie.setReleaseDate("2025.01.01");

        Movie movie = movieService.convertToMovie(tmdbMovie);

        assertNotNull(movie);
        assertEquals("Test Movie", movie.getTitle());
        assertEquals("Description", movie.getDescription());
        assertNull(movie.getRelease_date());
    }


}

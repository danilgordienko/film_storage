package ru.danilgordienko.film_storage.TmdbAPI;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.danilgordienko.film_storage.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TmdbApiTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TmdbClient tmdbClient;

    @Test
    public void testGetPopularMoviesReturnsList() {
        TmdbMovie movie1 = new TmdbMovie();
        movie1.setTitle("Movie 1");

        TmdbResponse response = new TmdbResponse();
        response.setResults(List.of(movie1));

        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        List<TmdbMovie> result = tmdbClient.getPopularMovies();

        assertEquals(1, result.size());
        assertEquals("Movie 1", result.getFirst().getTitle());
    }

    @Test
    public void testGetPopularMoviesReturnsEmptyListWhenNullResponse() {
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        List<TmdbMovie> result = tmdbClient.getPopularMovies();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetPopularMoviesReturnsEmptyListOnException() {
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class)))
                .thenThrow(new RestClientException("Network error"));

        List<TmdbMovie> result = tmdbClient.getPopularMovies();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testLoadGenresReturnsMappedGenres() {
        Genre genre1 = new Genre();
        genre1.setId(1L);
        genre1.setName("Action");

        TmdbClient.GenreResponse response = new TmdbClient.GenreResponse();
        response.setGenres(List.of(genre1));

        when(restTemplate.getForEntity(anyString(), eq(TmdbClient.GenreResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        List<Genre> result = tmdbClient.loadGenres();

        assertEquals(1, result.size());
        assertEquals("Action", result.getFirst().getName());
        assertEquals(1L, result.getFirst().getTmdbId());
    }

    @Test
    public void testLoadGenresReturnsEmptyListOnError() {
        when(restTemplate.getForEntity(anyString(), eq(TmdbClient.GenreResponse.class)))
                .thenThrow(new RestClientException("API down"));

        List<Genre> result = tmdbClient.loadGenres();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testLoadGenresReturnsEmptyListWhenResponseIsNull() {
        when(restTemplate.getForEntity(anyString(), eq(TmdbClient.GenreResponse.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        List<Genre> result = tmdbClient.loadGenres();

        assertTrue(result.isEmpty());
    }
}

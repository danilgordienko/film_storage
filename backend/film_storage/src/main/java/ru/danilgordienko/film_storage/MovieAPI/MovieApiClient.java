package ru.danilgordienko.film_storage.MovieAPI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import ru.danilgordienko.film_storage.model.Genre;
import ru.danilgordienko.film_storage.model.Movie;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieApiClient {

    private final String API_BASE_URL = "http://localhost:8082/api/tmdb";

    private final RestTemplate restTemplate;

    // URL для получения всех жанров из внешнего API
    private String getGenreUrl() {
        return API_BASE_URL + "/genres";
    }

    // URL для получения популярных фильмов из внешнего API
    private String getMovieUrl() {
        return API_BASE_URL + "/movies/popular";
    }

    // URL для получения популярных фильмов из внешнего API
    private String getRecentMovieUrl() {
        return API_BASE_URL + "/movies/recent";
    }

    public List<Movie> getPopularMovies() {
        try {
            ResponseEntity<List<Movie>> response = restTemplate.exchange(
                    getMovieUrl(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Movie>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Your API error: {}", e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Network error while accessing Your API: {}", e.getMessage());
        } catch (RestClientException e) {
            log.error("Unknown error while accessing Your API", e);
        }
        return List.of();
    }

    public List<Movie> getRecentMovies() {
        try {

            ResponseEntity<List<Movie>> response = restTemplate.exchange(
                    getRecentMovieUrl(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Movie>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Your API error: {}", e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Network error while accessing Your API: {}", e.getMessage());
        } catch (RestClientException e) {
            log.error("Unknown error while accessing Your API", e);
        }
        return List.of();
    }

    public byte[] downloadPoster(String posterPath) {
        log.info("Получение постера с url: {}", posterPath);
        if (posterPath == null || posterPath.isBlank()) {
            return new byte[0];
        }

        String imageUrl = API_BASE_URL + "/posters/" + posterPath;
        log.info("Fetching poster from: {}", imageUrl);

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    imageUrl,
                    byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody() != null ? response.getBody() : new byte[0];
            } else {
                log.warn("Poster loading error: status {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Error loading poster from {}: {}", imageUrl, e.getMessage());
        }
        return new byte[0];
    }

    public List<Genre> loadGenres() {
        try {
            ResponseEntity<List<Genre>> response = restTemplate.exchange(
                    getGenreUrl(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Genre>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Your API error: {}", e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Network error while accessing Your API: {}", e.getMessage());
        } catch (RestClientException e) {
            log.error("Unknown error while accessing Your API", e);
        }
        return List.of();
    }
}
package ru.danilgordienko.film_fetcher.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.danilgordienko.film_fetcher.model.dto.request.TmdbMovie;
import ru.danilgordienko.film_fetcher.service.MovieApiService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/tmdb")
public class TmdbApiController {

    private final MovieApiService tmdbApiService;

//    @GetMapping("/genres")
//    public Mono<List<Genre>> getGenres() {
//        return tmdbService.loadGenres();
//    }

    @GetMapping("/movies/popular")
    public Mono<List<TmdbMovie>> getPopularMovies() {
        log.info("GET /movies/popular, request received to fetch popular movies");
        var response = tmdbApiService.getPopularMovies();
        log.info("Popular movies fetched successfully");
        return response;
    }

    @GetMapping("/movies/recent")
    public Mono<List<TmdbMovie>> getRecentMovies() {
        log.info("GET /movies/recent, request received to fetch recently released movies");
        var response = tmdbApiService.getRecentlyReleasedMovies(7);
        log.info("Recently released movies fetched successfully");
        return response;
    }

    @GetMapping(value = "/posters/{posterPath}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadPoster(@PathVariable String posterPath) {
        log.info("GET /posters/{}, request received to fetch poster", posterPath);
        return tmdbApiService.downloadPoster(posterPath)
                .map(bytes -> {
                    if (bytes.length > 0) {
                        log.info("Poster {} fetched successfully", posterPath);
                        return ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_JPEG)
                                .body(bytes);
                    }
                    log.warn("Poster {} not found", posterPath);
                    return ResponseEntity.notFound().build();
                });
    }
}

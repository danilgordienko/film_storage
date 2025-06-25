package ru.danilgordienko.film_fetcher.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.danilgordienko.film_fetcher.model.Genre;
import ru.danilgordienko.film_fetcher.model.TmdbMovie;
import ru.danilgordienko.film_fetcher.service.TmdbService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/tmdb")
public class TmdbApiController {

    private final TmdbService tmdbService;

//    @GetMapping("/genres")
//    public Mono<List<Genre>> getGenres() {
//        return tmdbService.loadGenres();
//    }

    @GetMapping("/movies/popular")
    public Mono<List<TmdbMovie>> getPopularMovies() {
        return tmdbService.getPopularMovies();
    }

    @PostMapping("/movies/send/popular")
    public void populateMovies() {
        tmdbService.populateMovies();
    }

    @GetMapping("/movies/recent")
    public Mono<List<TmdbMovie>> getRecentMovies() {
        return tmdbService.getRecentlyReleasedMovies(7);
    }

    @GetMapping(value = "/posters/{posterPath}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadPoster(@PathVariable String posterPath) {
        log.info("Downloading image from {}", posterPath);
        return tmdbService.downloadPoster(posterPath)
                .map(bytes -> {
                    if (bytes.length > 0) {
                        return ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_JPEG)
                                .body(bytes);
                    }
                    return ResponseEntity.notFound().build();
                });
    }
}

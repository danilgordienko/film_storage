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
        return tmdbApiService.getPopularMovies();
    }

//    @PostMapping("/movies/send/popular")
//    public void populateMovies() {
//        tmdbApiService.populateMovies();
//    }

    @GetMapping("/movies/recent")
    public Mono<List<TmdbMovie>> getRecentMovies() {
        return tmdbApiService.getRecentlyReleasedMovies(7);
    }

    @GetMapping(value = "/posters/{posterPath}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadPoster(@PathVariable String posterPath) {
        log.info("Downloading image from {}", posterPath);
        return tmdbApiService.downloadPoster(posterPath)
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

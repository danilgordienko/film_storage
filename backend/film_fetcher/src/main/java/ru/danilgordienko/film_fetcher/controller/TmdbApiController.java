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
        log.info("GET /movies/popular, получен запрос на поллучение популярных фильмов");
        var response = tmdbApiService.getPopularMovies();
        log.info("Популярные фильмы успешно получены");
        return response;
    }

//    @PostMapping("/movies/send/popular")
//    public void populateMovies() {
//        tmdbApiService.populateMovies();
//    }

    @GetMapping("/movies/recent")
    public Mono<List<TmdbMovie>> getRecentMovies() {
        log.info("GET /movies/recent, получен запрос на поллучение недавно вышедших фильмов");
        var response = tmdbApiService.getRecentlyReleasedMovies(7);
        log.info("Недавно вышедшие фильмы успешно получены");
        return response;
    }

    @GetMapping(value = "/posters/{posterPath}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadPoster(@PathVariable String posterPath) {
        log.info("GET /poster/{}, получен запрос на получение постера", posterPath);
        return tmdbApiService.downloadPoster(posterPath)
                .map(bytes -> {
                    if (bytes.length > 0) {
                        log.info("Постер {} успешно получен", posterPath);
                        return ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_JPEG)
                                .body(bytes);
                    }
                    log.warn("Постер {} не найден",  posterPath);
                    return ResponseEntity.notFound().build();
                });
    }
}

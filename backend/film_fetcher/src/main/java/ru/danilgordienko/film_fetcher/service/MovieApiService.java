package ru.danilgordienko.film_fetcher.service;

import reactor.core.publisher.Mono;
import ru.danilgordienko.film_fetcher.model.dto.request.TmdbMovie;

import java.util.List;

public interface MovieApiService {

    Mono<byte[]> downloadPoster(String posterPath);
    Mono<List<TmdbMovie>> getRecentlyReleasedMovies(int days);
    Mono<List<TmdbMovie>> getPopularMovies();
}

package ru.danilgordienko.film_fetcher.service;

import reactor.core.publisher.Mono;
import ru.danilgordienko.film_fetcher.model.dto.request.Genre;
import ru.danilgordienko.film_fetcher.model.dto.response.MovieDto;

import java.util.List;

public interface MovieApiService {

    Mono<byte[]> downloadPoster(String posterPath);
    Mono<List<MovieDto>> getRecentlyReleasedMovies(int days);
    Mono<List<MovieDto>> getPopularMovies(int page);
    List<Genre> getGenres();
}

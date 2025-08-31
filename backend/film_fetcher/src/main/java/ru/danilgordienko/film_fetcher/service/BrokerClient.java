package ru.danilgordienko.film_fetcher.service;

import ru.danilgordienko.film_fetcher.model.dto.response.TmdbMovieResponse;

import java.util.List;

public interface BrokerClient {

    boolean sendMovies(List<TmdbMovieResponse> movies);
}

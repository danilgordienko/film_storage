package ru.danilgordienko.film_fetcher.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


//класс для хранения списка фильмов, полученных с tmdb
@Setter
@Getter
public class TmdbResponse {
    private List<TmdbMovie> results;
}

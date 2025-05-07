package ru.danilgordienko.film_storage.TmdbAPI;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


//класс для хранения списка фильмов, полученных с tmdb
@Setter
@Getter
public class TmdbResponse {
    private List<TmdbMovie> results;
}

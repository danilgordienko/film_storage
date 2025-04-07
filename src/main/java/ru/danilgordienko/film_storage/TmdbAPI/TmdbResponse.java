package ru.danilgordienko.film_storage.TmdbAPI;

import java.util.List;


//класс для хранения списка фильмов, полученных с tmdb
public class TmdbResponse {
    private List<TmdbMovie> results;

    public List<TmdbMovie> getResults() {
        return results;
    }

    public void setResults(List<TmdbMovie> results) {
        this.results = results;
    }
}

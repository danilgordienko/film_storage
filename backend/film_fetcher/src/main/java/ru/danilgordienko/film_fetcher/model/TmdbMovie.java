package ru.danilgordienko.film_fetcher.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;


//класс представляющий фильм, полученный с tmdb
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TmdbMovie {

    private String title;

    @JsonProperty("overview")
    private String description;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("poster_path")
    private String poster;

    @JsonProperty("genre_ids")
    private List<Long> genreIds;

    private List<Genre> genres;
}

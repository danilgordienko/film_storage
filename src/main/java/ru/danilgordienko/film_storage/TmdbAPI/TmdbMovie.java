package ru.danilgordienko.film_storage.TmdbAPI;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.danilgordienko.film_storage.model.Genre;

import java.util.List;
import java.util.Set;


//класс представляющий фильм, полученный с tmdb
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TmdbMovie {
    private String title;
    private String overview;
    @JsonProperty("release_date")  // Указываем, что поле в JSON называется "release_date"
    private String releaseDate;
    @JsonProperty("genre_ids")  // Указываем, что поле в JSON называется "genre_ids"
    private List<Long> genreIds;
    private Set<Genre> genres;
}

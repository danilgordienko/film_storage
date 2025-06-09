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
    @JsonProperty("release_date")
    private String releaseDate;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("genre_ids")
    private List<Long> genreIds;
    private Set<Genre> genres;
}

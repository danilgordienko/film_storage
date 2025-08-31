package ru.danilgordienko.film_fetcher.model.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.danilgordienko.film_fetcher.model.dto.request.Genre;

import java.util.List;


//класс представляющий фильм, полученный с tmdb
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {

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

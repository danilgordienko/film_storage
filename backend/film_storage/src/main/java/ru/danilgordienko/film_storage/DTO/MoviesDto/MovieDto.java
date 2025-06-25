package ru.danilgordienko.film_storage.DTO.MoviesDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.danilgordienko.film_storage.model.Genre;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
public class MovieDto {

    private Date release_date;

    @JsonProperty("overview")
    private String description;

    private String title;

    @JsonProperty("poster_path")
    private String poster;

    private Set<Genre> genres = new HashSet<>();
}

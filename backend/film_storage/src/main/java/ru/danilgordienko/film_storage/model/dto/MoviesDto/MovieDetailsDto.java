package ru.danilgordienko.film_storage.model.dto.MoviesDto;


import lombok.Data;
import ru.danilgordienko.film_storage.model.dto.RatingDto;

import java.util.*;

@Data
public class MovieDetailsDto {

    private String title;

    private String description;

    private Date release_date;

    private List<String> genres;

    private String poster;

    private List<RatingDto> ratings;
}

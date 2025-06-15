package ru.danilgordienko.film_storage.DTO.MoviesDto;


import lombok.Data;
import ru.danilgordienko.film_storage.DTO.RatingDto;

import java.util.*;

@Data
public class MovieDetailsDto {

    private String title;

    private String description;

    private Date release_date;

    private List<String> genres;

    private String posterUrl;

    private List<RatingDto> ratings;
}

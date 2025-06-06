package ru.danilgordienko.film_storage.DTO;


import lombok.Data;

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

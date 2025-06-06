package ru.danilgordienko.film_storage.DTO;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MovieListDto {
    private String title;

    private Date release_date;

    private String posterUrl;

    private List<String> genres;

    private int rating;
}

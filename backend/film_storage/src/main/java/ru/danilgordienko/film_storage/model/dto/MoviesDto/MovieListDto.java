package ru.danilgordienko.film_storage.model.dto.MoviesDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MovieListDto {
    private Long id;

    private String title;

    private Date release_date;

    private String poster;

    private List<String> genres;

    private double rating;
}

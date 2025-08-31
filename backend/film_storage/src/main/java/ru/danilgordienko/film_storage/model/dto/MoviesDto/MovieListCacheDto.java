package ru.danilgordienko.film_storage.model.dto.MoviesDto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieListCacheDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date release_date;

    private String poster;
    private List<String> genres;
    private double rating;
}
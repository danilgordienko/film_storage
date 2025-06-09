package ru.danilgordienko.film_storage.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.List;

@Document(indexName = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDocument {
    private Long id;

    private String title;

    private Date release_date;

    private List<String> genres;

    private double averageRating;
}

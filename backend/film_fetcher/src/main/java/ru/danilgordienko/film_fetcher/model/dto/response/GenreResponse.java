package ru.danilgordienko.film_fetcher.model.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreResponse {
    private Long tmdbId;

    private String name;
}
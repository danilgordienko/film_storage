package ru.danilgordienko.film_storage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private Long id;
    private Long movieId;
    private Long senderId;
    private Long receiverId;
}

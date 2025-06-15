package ru.danilgordienko.film_storage.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieNameDto;

import java.time.LocalDateTime;

@Data
public class RatingDto {

    private String username;

    private MovieNameDto movie;

    @NotNull(message = "Рейтинг обязателен")
    @Min(value = 1, message = "Рейтинг должен быть минимум 1")
    @Max(value = 10, message = "Рейтинг не может быть больше 10")
    private int rating;

    private String comment;

    private LocalDateTime createdAt;
}

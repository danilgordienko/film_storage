package ru.danilgordienko.film_storage.DTO.UsersDto;

import lombok.Data;
import ru.danilgordienko.film_storage.DTO.RatingDto;

import java.util.List;

@Data
public class UserRatingDto {

    List<RatingDto> ratings;
}

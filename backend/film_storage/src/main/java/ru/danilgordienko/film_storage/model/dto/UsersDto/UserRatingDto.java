package ru.danilgordienko.film_storage.model.dto.UsersDto;

import lombok.Data;
import ru.danilgordienko.film_storage.model.dto.RatingDto;

import java.util.List;

@Data
public class UserRatingDto {

    List<RatingDto> ratings;
}

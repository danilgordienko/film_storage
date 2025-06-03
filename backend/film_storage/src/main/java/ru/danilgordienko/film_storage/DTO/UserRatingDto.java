package ru.danilgordienko.film_storage.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserRatingDto {

    List<RatingDto> ratings;
}

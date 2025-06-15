package ru.danilgordienko.film_storage.DTO.UsersDto;

import lombok.Data;
import ru.danilgordienko.film_storage.DTO.FavoriteDto;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.model.Favorite;

import java.util.List;

@Data
public class UserFavoritesDto {

    List<FavoriteDto> favorites;
}

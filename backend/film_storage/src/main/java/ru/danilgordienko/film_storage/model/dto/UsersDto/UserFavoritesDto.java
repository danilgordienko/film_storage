package ru.danilgordienko.film_storage.model.dto.UsersDto;

import lombok.Data;
import ru.danilgordienko.film_storage.model.dto.FavoriteDto;

import java.util.List;

@Data
public class UserFavoritesDto {

    List<FavoriteDto> favorites;
}

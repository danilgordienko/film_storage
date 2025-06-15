package ru.danilgordienko.film_storage.DTO.mapping;

import org.mapstruct.Mapper;
import ru.danilgordienko.film_storage.DTO.FavoriteDto;
import ru.danilgordienko.film_storage.model.Favorite;

@Mapper(componentModel = "spring", uses = MovieMapping.class)
public interface FavoriteMapping {
    FavoriteDto mapping(Favorite favorite);
}

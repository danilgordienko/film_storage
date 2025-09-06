package ru.danilgordienko.film_storage.model.dto.mapping;

import org.mapstruct.Mapper;
import ru.danilgordienko.film_storage.model.dto.FavoriteDto;
import ru.danilgordienko.film_storage.model.entity.Favorite;

@Mapper(componentModel = "spring", uses = MovieMapping.class)
public interface FavoriteMapping {
    FavoriteDto mapping(Favorite favorite);
}

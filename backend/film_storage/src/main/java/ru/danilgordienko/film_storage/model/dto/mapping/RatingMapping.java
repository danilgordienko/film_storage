package ru.danilgordienko.film_storage.model.dto.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.film_storage.model.dto.RatingDto;
import ru.danilgordienko.film_storage.model.entity.Rating;

@Mapper(componentModel = "spring")
public interface RatingMapping {

    @Mapping(target = "username", expression = "java(rating.getUser().getUsername())")
    @Mapping(source = "createdAt", target = "createdAt")
    RatingDto toRatingMapping(Rating rating);
}

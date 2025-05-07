package ru.danilgordienko.film_storage.DTO.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.film_storage.DTO.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.Rating;

@Mapper(componentModel = "spring")
public interface RatingMapping {

    @Mapping(target = "username", expression = "java(rating.getUser().getUsername())")
    @Mapping(source = "createdAt", target = "createdAt")
    RatingDto toRatingMapping(Rating rating);
}

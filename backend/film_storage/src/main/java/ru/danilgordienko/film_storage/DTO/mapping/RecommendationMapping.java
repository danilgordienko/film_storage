package ru.danilgordienko.film_storage.DTO.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.film_storage.DTO.RecommendationDto;
import ru.danilgordienko.film_storage.model.Recommendation;

@Mapper(componentModel = "spring")
public interface RecommendationMapping {

    @Mapping(target = "senderId", expression = "java(recommendation.getSender().getId())")
    @Mapping(target = "receiverId", expression = "java(recommendation.getReceiver().getId())")
    @Mapping(target = "movieId", expression = "java(recommendation.getMovie().getId())")
    RecommendationDto toRecommendationDto(Recommendation recommendation);

}


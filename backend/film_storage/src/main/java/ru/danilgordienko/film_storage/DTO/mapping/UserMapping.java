package ru.danilgordienko.film_storage.DTO.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.film_storage.DTO.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.UserRatingDto;
import ru.danilgordienko.film_storage.model.User;

@Mapper(componentModel = "spring", uses = RatingMapping.class)
public interface UserMapping {

    UserInfoDto toUserInfoDto(User user);

    UserRatingDto toUserRatingDto(User user);

    UserFriendsDto toUserFriendsDto(User user);
}

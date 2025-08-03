package ru.danilgordienko.film_storage.DTO.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.film_storage.DTO.UsersDto.*;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.model.UserDocument;

@Mapper(componentModel = "spring", uses = {RatingMapping.class, FavoriteMapping.class})
public interface UserMapping {

    @Mapping(target = "id", expression = "java(user.getId())")
    UserInfoDto toUserInfoDto(User user);

    @Mapping(target = "id", expression = "java(user.getId())")
    UserInfoDto toUserInfoDto(UserDocument user);

    @Mapping(target = "id", expression = "java(user.getId())")
    UserListDto toUserListDto(UserDocument user);

    UserRatingDto toUserRatingDto(User user);

    UserFriendsDto toUserFriendsDto(User user);

    @Mapping(target = "id", expression = "java(user.getId())")
    UserDocument toUserDocument(User user);

    UserFavoritesDto toUserFavoritesDto(User user);

    @Mapping(target = "id", expression = "java(user.getId())")
    UserListDto  toUserListDto(User user);
}

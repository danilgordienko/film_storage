package ru.danilgordienko.film_storage.DTO.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import ru.danilgordienko.film_storage.DTO.PageDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.*;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.model.UserDocument;

import java.util.List;
import java.util.stream.Collectors;

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


    PageDto<UserListDto>  toUserListDtoPage(Page<User> page);

    @Mapping(target = "content", source = "page", qualifiedByName = "getContent")
    PageDto<UserListDto>  toUserListDtoPageFromUserDocument(Page<UserDocument> page);

    @Named("getContent")
    default List<UserListDto> getContent(Page<UserDocument> page) {
        if(page.getContent().isEmpty()) {
            return List.of();
        }
        return  page.getContent().stream().map(this::toUserListDto).toList();
    }
}

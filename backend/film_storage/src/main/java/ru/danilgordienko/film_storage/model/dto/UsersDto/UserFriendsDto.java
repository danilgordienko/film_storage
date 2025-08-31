package ru.danilgordienko.film_storage.model.dto.UsersDto;

import lombok.Data;

import java.util.List;

@Data
public class UserFriendsDto {
    private List<UserListDto> friends;
}

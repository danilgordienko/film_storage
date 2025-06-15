package ru.danilgordienko.film_storage.DTO.UsersDto;

import lombok.Data;

import java.util.List;

@Data
public class UserFriendsDto {
    private List<UserInfoDto> friends;
}

package ru.danilgordienko.film_storage.DTO;

import lombok.Data;

import java.util.List;

@Data
public class UserFriendsDto {
    private List<UserInfoDto> friends;
}

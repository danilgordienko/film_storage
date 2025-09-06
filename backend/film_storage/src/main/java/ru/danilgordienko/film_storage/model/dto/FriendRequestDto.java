package ru.danilgordienko.film_storage.model.dto;

import lombok.Data;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserInfoDto;

@Data
public class FriendRequestDto {
    private UserInfoDto sender;

    private UserInfoDto receiver;
}

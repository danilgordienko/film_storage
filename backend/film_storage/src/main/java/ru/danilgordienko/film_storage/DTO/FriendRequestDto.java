package ru.danilgordienko.film_storage.DTO;

import lombok.Data;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserInfoDto;

@Data
public class FriendRequestDto {
    private UserInfoDto sender;

    private UserInfoDto receiver;
}

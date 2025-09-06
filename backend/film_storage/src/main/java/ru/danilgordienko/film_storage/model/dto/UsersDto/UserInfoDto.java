package ru.danilgordienko.film_storage.model.dto.UsersDto;

import lombok.Data;

@Data
public class UserInfoDto {
    private Long id;
    private String email;
    private String username;
    private byte[] avatar;
}

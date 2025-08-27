package ru.danilgordienko.film_storage.DTO.UsersDto;

import lombok.Data;

@Data
public class UserListDto {
    private Long id;
    private String username;
    private byte[] avatar;
}

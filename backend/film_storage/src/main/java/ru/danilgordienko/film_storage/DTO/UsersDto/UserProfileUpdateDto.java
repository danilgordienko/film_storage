package ru.danilgordienko.film_storage.DTO.UsersDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserProfileUpdateDto {
    //@NotBlank(message = "Логин не может быть пустым")
    private String username;
    //@NotBlank(message = "Email не может быть пустым")
    @Email
    private String email;
}

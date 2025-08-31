package ru.danilgordienko.film_storage.model.dto.UsersDto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateDto {
    private String username;
    @Email
    private String email;

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return this.username;
    }

}

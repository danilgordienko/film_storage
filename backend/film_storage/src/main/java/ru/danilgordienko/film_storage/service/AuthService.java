package ru.danilgordienko.film_storage.service;

import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.model.dto.AuthResponse;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserLoginDto;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserRegistrationDTO;

@Service
public interface AuthService {
    AuthResponse login(UserLoginDto loginRequest);
    AuthResponse register(UserRegistrationDTO registerRequest);
    AuthResponse refresh(String header);
    void logout(String header);
    void addAdminRole(String login);
}

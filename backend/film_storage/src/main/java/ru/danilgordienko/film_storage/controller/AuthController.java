package ru.danilgordienko.film_storage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.film_storage.DTO.AuthResponse;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserLoginDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRegistrationDTO;
import ru.danilgordienko.film_storage.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Аутентификация пользователя и выдача JWT-токена
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid UserLoginDto loginDto) {
        log.info("Попытка входа пользователя: {}", loginDto.getUsername());
        AuthResponse response = authService.login(loginDto);
        log.info("Успешная аутентификация: {}", loginDto.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegistrationDTO user) {
        log.info("Попытка регистрации пользователя: {}", user.getUsername());
        AuthResponse response = authService.register(user);
        log.info("Пользователь успешно зарегистрирован: {}", user.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("token/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Received token refresh request");
        return ResponseEntity.ok(authService.refresh(authHeader));
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Received logout request");
        authService.logout(authHeader);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/admin/{login}")
    public ResponseEntity<String> assignAdminRole(@PathVariable String login) {
        authService.addAdminRole(login);
        return ResponseEntity.ok("Admin role assigned to user " + login);
    }
}

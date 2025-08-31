package ru.danilgordienko.film_storage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.film_storage.model.dto.AuthResponse;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserLoginDto;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserRegistrationDTO;
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
        log.info("[POST /api/auth/login] User login attempt: {}", loginDto.getUsername());
        AuthResponse response = authService.login(loginDto);
        log.info("[POST /api/auth/login] Successful authentication: {}", loginDto.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegistrationDTO user) {
        log.info("[POST /api/auth/register] User registration attempt: {}", user.getUsername());
        AuthResponse response = authService.register(user);
        log.info("[POST /api/auth/register] User successfully registered: {}", user.getUsername());
        return ResponseEntity.ok(response);
    }

    /*
     * Обновление access токена по refresh токену
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        log.info("[POST /api/auth/token/refresh] Received token refresh request");
        var response = authService.refresh(authHeader);
        log.info("[POST /api/auth/token/refresh] Successfuly refresh token");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authHeader) {
        log.info("[POST /api/auth/logout] Received logout request");
        authService.logout(authHeader);
        log.info("[POST /api/auth/logout] Successfully logged out");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/admin/{login}")
    public ResponseEntity<String> assignAdminRole(@PathVariable String login) {
        log.info("[POST /api/auth/add/admin/{}] Assigning admin role", login);
        authService.addAdminRole(login);
        log.info("[POST /api/auth/add/admin/{}] Admin role successfully assigned", login);
        return ResponseEntity.ok("Admin role assigned to user " + login);
    }
}

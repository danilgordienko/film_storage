package ru.danilgordienko.film_storage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.danilgordienko.film_storage.DTO.UserRegistrationDTO;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.security.JWTCore;
import ru.danilgordienko.film_storage.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTCore jwtCore;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Аутентификация пользователя и выдача JWT-токена
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        log.info("Попытка входа пользователя: {}", user.getUsername());
        try {
            // Создаем объект аутентификации с переданными учетными данными
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            // Устанавливаем аутентифицированного пользователя в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Генерируем JWT-токен
            String jwt = jwtCore.generateToken(authentication);
            log.info("Успешная аутентификация: {}", user.getUsername());
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            log.warn("Неверные учетные данные для пользователя: {}", user.getUsername());
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (UsernameNotFoundException e) {
            log.warn("Пользователь не найден: {}", user.getUsername());
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            log.error("Ошибка при генерации токена для пользователя: {}", user.getUsername(), e);
            return ResponseEntity.status(500).body("Error generating token");
        }

    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegistrationDTO user, BindingResult result) {
        log.info("Попытка регистрации пользователя: {}", user.getUsername());

        try {
            //проверяем есть ли ошибки при вводе логина и пароля
            if (result.hasErrors()) {
                String errorMessage = result.getAllErrors().getFirst().getDefaultMessage();
                log.warn("Ошибка валидации при регистрации пользователя {}: {}", user.getUsername(), errorMessage);
                return ResponseEntity.badRequest().body(errorMessage);
            }

            if (userService.existsUser(user.getUsername())) {
                log.warn("Пользователь уже существует: {}", user.getUsername());
                return ResponseEntity.status(409).body("User already exists");
            }
            // Кодируем пароль перед сохранением
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.addUser(user);
            log.info("Пользователь успешно зарегистрирован: {}", user.getUsername());
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя: {}", user.getUsername(), e);
            return ResponseEntity.status(400).body("Error registering user: " + e.getMessage());
        }
    }
}

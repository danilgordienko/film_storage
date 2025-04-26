package ru.danilgordienko.film_storage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        Authentication authentication = null;
        try {
            // Создаем объект аутентификации с переданными учетными данными
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            // Устанавливаем аутентифицированного пользователя в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Генерируем JWT-токен
            String jwt = jwtCore.generateToken(authentication);
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            return  ResponseEntity.status(500).body("Error generating token");
        }

    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegistrationDTO user, BindingResult result) {
        try {
            //проверяем есть ли ошибки при вводе логина и пароля
            if (result.hasErrors()) {
                String errorMessage = result.getAllErrors().getFirst().getDefaultMessage();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            if (userService.existsUser(user.getUsername())) {
                return ResponseEntity.status(409).body("User already exists");
            }
            // Кодируем пароль перед сохранением
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.addUser(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error registering user: " + e.getMessage());
        }
    }
}

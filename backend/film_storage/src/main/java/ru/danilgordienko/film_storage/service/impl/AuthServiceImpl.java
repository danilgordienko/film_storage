package ru.danilgordienko.film_storage.service.impl;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.danilgordienko.film_storage.model.dto.AuthResponse;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserLoginDto;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserRegistrationDTO;
import ru.danilgordienko.film_storage.model.dto.mapping.UserMapping;
import ru.danilgordienko.film_storage.exception.*;
import ru.danilgordienko.film_storage.model.enums.Role;
import ru.danilgordienko.film_storage.model.entity.User;
import ru.danilgordienko.film_storage.repository.UserRepository;
import ru.danilgordienko.film_storage.repository.UserSearchRepository;
import ru.danilgordienko.film_storage.security.JWTCore;
import ru.danilgordienko.film_storage.security.UserDetailsImpl;
import ru.danilgordienko.film_storage.service.AuthService;

import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Qualifier("userService")
    private final UserDetailsService userDetailsService;
    private final UserSearchRepository  userSearchRepository;
    private final UserMapping userMapping;
    private final JWTCore jwtService;


    // регисрация админа по умолчанию(временно)
    @PostConstruct
    public void initAdmin() {
        String defaultAdminLogin = "admin";
        boolean adminExists = userRepository.existsByUsername(defaultAdminLogin);
        if (!adminExists) {
            User admin = User.builder()
                    .username(defaultAdminLogin)
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(Role.ADMIN.name()))
                    .build();
            userRepository.save(admin);
        }
    }


    // аунтификация пользователя по учетным данным
    @Transactional
    public AuthResponse login(UserLoginDto loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getUsername());
        // аунтфицируем по логину и паролю
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        var user = getUserByUsername(loginRequest.getUsername());
        ;        // генерируем токены
        var accessToken = jwtService.generateAccessToken(UserDetailsImpl.build(user));
        var  refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
        // удаляем все старые токены юзера
        jwtService.deleteAllRefreshTokensByUser(user);
        jwtService.deleteAllAccessTokensByUser(user);
        // сохраняем refresh токен
        jwtService.saveRefreshToken(refreshToken, user);
        jwtService.saveAccessToken(accessToken, user);
        return new  AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse register(UserRegistrationDTO registerRequest) {
        try {
            log.info("Registering new user: {}", registerRequest.getUsername());
            User user = User.builder()
                    .email(registerRequest.getEmail())
                    .username(registerRequest.getUsername())
                    .password(passwordEncoder.encode(registerRequest.getPassword())) // кодируем пароль
                    .roles(Set.of(Role.USER.name()))
                    .build();
            userRepository.save(user);
            userSearchRepository.save(userMapping.toUserDocument(user));
            // генерируем токены
            var accessToken = jwtService.generateAccessToken(UserDetailsImpl.build(user));
            var refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
            // сохраняем refresh токен
            jwtService.saveRefreshToken(refreshToken, user);
            jwtService.saveAccessToken(accessToken, user);
            return new AuthResponse(accessToken, refreshToken);
        } catch (DataAccessException e) {
            log.error("Database save error", e);
            throw new DatabaseConnectionException("Failed to save user in database", e);
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Elasticsearch save error", e);
            throw new ElasticsearchConnectionException("Failed to save user in Elasticsearch", e);
        } catch (Exception e) {
            log.error("User registration error", e);
            throw new UserRegistrationException("Unexpected error during user registration", e);
        }
    }

    // получение нового access токена
    @Override
    public AuthResponse refresh(String header) {
        log.info("Attempting to refresh access token using refresh token");
        String refreshToken = jwtService.getTokenFromHeader(header);

        // Извлекаем имя пользователя из токена
        String username = jwtService.getUsernameFromToken(refreshToken);
        log.debug("Extracted username from refresh token: {}", username);

        // Загружаем данные пользователя по логину
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        jwtService.validateRefreshToken(refreshToken);

        User user = getUserByUsername(username);
        var accessToken = jwtService.generateAccessToken(userDetails);
        jwtService.saveAccessToken(accessToken, user);
        var authResponse = new AuthResponse(accessToken, refreshToken);
        log.info("Access token refreshed for user: {}", username);
        return authResponse;
    }

    // отзыв refresh токена
    @Transactional
    public void logout(String header) {
        log.info("Attempting logout with refresh token");
        String refreshToken = jwtService.getTokenFromHeader(header);

        jwtService.validateRefreshToken(refreshToken);

        jwtService.revokeRefreshToken(refreshToken);
        log.info("Tokens revoked");
    }

    // получение пользователя по логину
    private User getUserByUsername(String username) {
        try {
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        log.warn("User not found: {}", username);
                        return new UserNotFoundException("User " +
                                username +
                                "not found");
                    });
        }   catch (DataAccessException e) {
            log.error("Database connection error", e);
            throw new DatabaseConnectionException("Failed to connect to database", e);
        }
    }

    @Transactional
    public void addAdminRole(String login) {
        try {
            User user = getUserByUsername(login);
            if (user.getRoles().contains(Role.ADMIN.name())) {
                throw new IllegalStateException("User " + login + " is already an administrator");
            }
            user.getRoles().add(Role.ADMIN.name());
            userRepository.save(user);
        }  catch (DataAccessException e) {
            log.debug("Database connection error", e);
            throw new DatabaseConnectionException("Failed to connect to database", e);
        }
    }

}

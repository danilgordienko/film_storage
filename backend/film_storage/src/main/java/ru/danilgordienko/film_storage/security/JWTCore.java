package ru.danilgordienko.film_storage.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.TokenAlreadyRevokedException;
import ru.danilgordienko.film_storage.exception.TokenExpiredException;
import ru.danilgordienko.film_storage.exception.TokenNotFoundException;
import ru.danilgordienko.film_storage.model.AccessToken;
import ru.danilgordienko.film_storage.model.RefreshToken;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.AccessTokenRepository;
import ru.danilgordienko.film_storage.repository.RefreshTokenRepository;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTCore {

    // Секретный ключ для подписи JWT
    @Value("${app.security.secret}")
    private String secret;
    // Время жизни токена
    @Value("${app.security.access-expiration}")
    private int accessExpiration;

    @Value("${app.security.refresh-expiration}")
    private int refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenRepository accessTokenRepository;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // генерирует access токен
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails,  accessExpiration, "access");
    }

    // генерирует refresh toker
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails,  refreshExpiration, "refresh");
    }

    // Генерирует JWT-токен для аутентифицированного пользователя
    private String buildToken(UserDetails userDetails, int expiration, String tokenType) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) userDetails;

            return Jwts.builder()
                    .setSubject(user.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .claim("id", user.getId())
                    .claim("email", user.getEmail())
                    .claim("roles", user.getAuthorities().stream()
                            .map(Object::toString)
                            .toList())
                    .claim("token_type", tokenType)
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // проверки что refresh токен корректный
    public void validateRefreshToken(String token) {
        var storedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Invalid refresh token"));
        // не просрочен
        if (isTokenExpired(token)) {
            throw new TokenExpiredException("Refresh token is expired");
        }
        //не отозван
        if (storedToken.isRevoked()) {
            throw new TokenAlreadyRevokedException("Token already revoked");
        }
    }

    // проверки что refresh токен корректный
    public void validateAccessToken(String token) {
        var storedToken = accessTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Invalid access token"));
        // не просрочен
        if (isTokenExpired(token)) {
            throw new TokenExpiredException("Access token is expired");
        }
//        //не отозван
//        if (storedToken.isRevoked()) {
//            throw new TokenAlreadyRevokedException("Token already revoked");
//        }
    }

    // сохраняет refresh токен в бд
    public void saveRefreshToken(String refreshToken, User user)
    {
        try {
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .token(refreshToken)
                            .user(user)
                            .build()
            );
        } catch (DataAccessException e) {
            log.error("Ошибка подключения к БД", e);
            throw new DatabaseConnectionException("Не удалось подключится к БД", e);
        }
    }

    // сохраняет access токен в бд
    public void saveAccessToken(String accessToken, User user)
    {
        try {
            accessTokenRepository.save(
                    AccessToken.builder()
                            .token(accessToken)
                            .user(user)
                            .build()
            );
        } catch (DataAccessException e) {
            log.error("Ошибка подключения к БД", e);
            throw new DatabaseConnectionException("Не удалось подключится к БД", e);
        }
    }

    // помечает refresh токен у пользователя как отозванный
    public void revokeRefreshToken(String token) {
        try {
            refreshTokenRepository.findByToken(token).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
        } catch (DataAccessException e) {
            log.error("Ошибка подключения к БД", e);
            throw new DatabaseConnectionException("Не удалось подключится к БД", e);
        }
    }

//    // помечает refresh токен у пользователя как отозванный
//    public void revokeAccessToken(String token) {
//        try {
//            accessTokenRepository.findByToken(token).ifPresent(rt -> {
//                rt.setRevoked(true);
//                accessTokenRepository.save(rt);
//            });
//
//        } catch (DataAccessException e) {
//            log.error("Ошибка подключения к БД", e);
//            throw new DatabaseConnectionException("Не удалось подключится к БД", e);
//        }
//    }

    public void deleteAllRefreshTokensByUser(User user){
        try {
            refreshTokenRepository.deleteAllByUser(user);
        } catch (DataAccessException e) {
            log.error("Ошибка подключения к БД", e);
            throw new DatabaseConnectionException("Не удалось подключится к БД", e);
        }
    }

    public void deleteAllAccessTokensByUser(User user){
        try {
            accessTokenRepository.deleteAllByUser(user);
        } catch (DataAccessException e) {
            log.error("Ошибка подключения к БД", e);
            throw new DatabaseConnectionException("Не удалось подключится к БД", e);
        }
    }

    // проверка просрочен ли токен
    public boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

    // получение ролей из токена
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("roles", List.class);
    }

    // получение времени просрочки токена
    private Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // получение токена из заголовка
    public String getTokenFromHeader(String header){
        // Проверяем, что заголовок начинается с "Bearer " и извлекаем сам токен
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    // Извлекает тип из переданного JWT-токена
    public String getTypeFromToken(String token) {
        return getClaimsFromToken(token).get("token_type", String.class);
    }

    // Извлекает логин из переданного JWT-токена
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // Извлекает email из переданного JWT-токена
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }
}

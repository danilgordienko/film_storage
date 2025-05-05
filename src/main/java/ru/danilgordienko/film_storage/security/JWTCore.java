package ru.danilgordienko.film_storage.security;


import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTCore {

    // Секретный ключ для подписи JWT
    @Value("${app.secret}")
    private String secret;
    // Время жизни токена
    @Value("${app.expiration}")
    private int lifetime;

    /**
     * Генерирует JWT-токен для аутентифицированного пользователя
     */
    public String generateToken(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // Устанавливаем имя пользователя в токен
                .setIssuedAt(new Date()) // Устанавливаем дату создания токена
                .setExpiration(new Date(new Date().getTime() + lifetime)) // Устанавливаем дату истечения
                .signWith(SignatureAlgorithm.HS256, secret) // Подписываем токен секретным ключом
                .compact();
    }

    /**
     * Извлекает имя пользователя из переданного JWT-токена
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}

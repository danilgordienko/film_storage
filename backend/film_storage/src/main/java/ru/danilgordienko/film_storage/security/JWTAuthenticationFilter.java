package ru.danilgordienko.film_storage.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.danilgordienko.film_storage.exception.TokenAlreadyRevokedException;
import ru.danilgordienko.film_storage.exception.TokenExpiredException;
import ru.danilgordienko.film_storage.exception.TokenNotFoundException;

import java.io.IOException;
import java.security.SignatureException;

@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTCore jwtCore;
    private final UserDetailsService userDetailsService;

    public JWTAuthenticationFilter(JWTCore jwtCore, UserDetailsService userDetailsService) {
        this.jwtCore = jwtCore;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Метод вызывается для каждого запроса.
     * Проверяет наличие и валидность JWT-токена, аутентифицирует пользователя.
     *
     * @param request     HTTP-запрос.
     * @param response    HTTP-ответ.
     * @param filterChain цепочка фильтров, которые должны выполняться после этого.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Получаем JWT-токен из запроса
            String token = getTokenFromRequest(request);
            if (token != null) {
                String type = jwtCore.getTypeFromToken(token);
                if (type.equalsIgnoreCase("access"))
                    jwtCore.validateAccessToken(token);
                else
                    jwtCore.validateRefreshToken(token);
                // Извлекаем имя пользователя из токена
                String email = jwtCore.getEmailFromToken(token);
                log.debug("Received token for user: {}", email);

                // Загружаем данные пользователя по имени
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Создаем объект аутентификации и устанавливаем его в SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("User '{}' successfully authenticated in context", email);
            }

            // Передаем управление следующему фильтру в цепочке
            filterChain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            log.warn(e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT-token is expired");
        } catch (UnsupportedJwtException | MalformedJwtException | TokenAlreadyRevokedException | TokenNotFoundException e) {
            log.warn("Invalid JWT-token: {} ", e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT-token");
        } catch (JwtException e) {
            log.warn("Invalid JWT-token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT-token");
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication error");
        }
    }


    /**
     * Извлекает JWT-токен из заголовка Authorization.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Проверяем, что заголовок начинается с "Bearer " и извлекаем сам токен
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

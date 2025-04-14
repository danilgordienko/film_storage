package ru.danilgordienko.film_storage.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SignatureException;


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
                // Извлекаем имя пользователя из токена
                String username = jwtCore.getUsernameFromToken(token);

                // Загружаем данные пользователя по имени
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Создаем объект аутентификации и устанавливаем его в SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // Передаем управление следующему фильтру в цепочке
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            System.err.println("JWT-токен просрочен: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT-токен просрочен");
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            System.err.println("Недействительный JWT-токен: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Недействительный JWT-токен");
        } catch (JwtException e) {
            System.err.println("Неверный JWT-токен: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Неверный JWT-токен");
        } catch (Exception e) {
            System.err.println("Ошибка аутентификации: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ошибка аутентификации");
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

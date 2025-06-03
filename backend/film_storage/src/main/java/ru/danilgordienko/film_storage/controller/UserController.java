package ru.danilgordienko.film_storage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.danilgordienko.film_storage.DTO.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.UserRatingDto;
import ru.danilgordienko.film_storage.service.UserService;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}/info")
    public ResponseEntity<UserInfoDto> getUserInfo(@PathVariable Long id){
        log.info("Запрос данных о пользователе с id: {}", id);

        return userService.getUserInfo(id)
                .map(user -> {
                    log.info("Пользователь {} найден", user.getUsername());
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("Пользователь с id: {} не найден", id);
                    return  ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<UserRatingDto> getUserRatings(@PathVariable Long id){
        log.info("Запрос оценок пользователя с id: {}", id);

        return userService.getUserRatings(id)
                .map(user -> {
                    log.info("Пользователь найден, всего оценок: {}", user.getRatings().size());
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("Пользователь с id: {} не найден", id);
                    return  ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<UserFriendsDto> getUserFriends(@PathVariable Long id){
        log.info("Запрос друзей пользователя с id: {}", id);

        return userService.getUserFriends(id)
                .map(user -> {
                    log.info("Пользователь найден, всего друзей: {}", user.getFriends().size());
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("Пользователь с id: {} не найден", id);
                    return  ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/me/info")
    public ResponseEntity<UserInfoDto> getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос данных о текущем пользователе: {}", userDetails.getUsername());

        return userService.getUserInfoByUsername(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Пользователь с username: {} не найден", userDetails.getUsername());
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/me/ratings")
    public ResponseEntity<UserRatingDto> getCurrentUserRatings(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос оценок текущего пользователя: {}", userDetails.getUsername());

        return userService.getUserRatingsByUsername(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Оценки пользователя с username: {} не найдены", userDetails.getUsername());
                    return ResponseEntity.notFound().build();
                });
    }

}

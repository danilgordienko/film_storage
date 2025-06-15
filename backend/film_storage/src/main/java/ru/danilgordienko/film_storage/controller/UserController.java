package ru.danilgordienko.film_storage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserInfoDto;
import ru.danilgordienko.film_storage.service.UserService;

import java.util.List;

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

    // поиск пользователя по запросу query
    @GetMapping("/search")
    public ResponseEntity<List<UserInfoDto>> searchUsers(String query) {
        log.info("GET /api/users/search - поиск пользователей по запросу: {}", query);

        List<UserInfoDto> users = userService.searchUserByUsername(query);

        if (users.isEmpty()) {
            log.warn("По запросу '{}' пользователи не найдены", query);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("По запросу '{}' найдено {} пользователей", query, users.size());
        return ResponseEntity.ok(users);
    }

}

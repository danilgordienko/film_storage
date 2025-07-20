package ru.danilgordienko.film_storage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
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

    // получение инфо пользователя по id
    @GetMapping("/{id}/info")
    public ResponseEntity<UserInfoDto> getUserInfo(@PathVariable Long id) {
        log.info("Запрос данных о пользователе с id: {}", id);

        UserInfoDto userInfoDto = userService.getUserInfo(id);
        log.info("Пользователь {} найден", userInfoDto.getUsername());

        return ResponseEntity.ok(userInfoDto);
    }

    // получение друзей пользователя по id
    @GetMapping("/{id}/friends")
    public ResponseEntity<UserFriendsDto> getUserFriends(@PathVariable Long id) {
        log.info("Запрос друзей пользователя с id: {}", id);

        UserFriendsDto friendsDto = userService.getUserFriends(id);
        log.info("Пользователь найден, всего друзей: {}", friendsDto.getFriends().size());

        return ResponseEntity.ok(friendsDto);
    }

    // получение инфо пользователя текущего пользователя
    @GetMapping("/me/info")
    public ResponseEntity<UserInfoDto> getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос данных о текущем пользователе: {}", userDetails.getUsername());

        UserInfoDto userInfoDto = userService.getUserInfoByUsername(userDetails.getUsername());
        return ResponseEntity.ok(userInfoDto);
    }

    // поиск пользователя по запросу query
    @GetMapping("/search")
    public ResponseEntity<List<UserInfoDto>> searchUsers(@RequestParam String query) {
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

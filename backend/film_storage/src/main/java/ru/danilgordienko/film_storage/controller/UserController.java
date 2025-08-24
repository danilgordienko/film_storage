package ru.danilgordienko.film_storage.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.danilgordienko.film_storage.DTO.UsersDto.*;
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

    // получение инфо пользователя текущего пользователя
    @GetMapping("/me/info")
    public ResponseEntity<UserInfoDto> getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос данных о текущем пользователе: {}", userDetails.getUsername());

        UserInfoDto userInfoDto = userService.getUserInfoByUsername(userDetails.getUsername());
        return ResponseEntity.ok(userInfoDto);
    }

    // поиск пользователя по запросу query
    @GetMapping("/search")
    public ResponseEntity<List<UserListDto>> searchUsers(@RequestParam("query") String query) {
        log.info("GET /api/users/search - поиск пользователей по запросу: {}", query);

        List<UserListDto> users = userService.searchUserByUsername(query);

        if (users.isEmpty()) {
            log.warn("По запросу '{}' пользователи не найдены", query);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("По запросу '{}' найдено {} пользователей", query, users.size());
        return ResponseEntity.ok(users);
    }

    @PostMapping("me/profile/update")
    public ResponseEntity<String> updataProfile(
            @ModelAttribute @Valid UserProfileUpdateDto dto,
            @RequestParam(value = "avatar", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/users/me/profile/update - запрос на обновление профиля пользователя: {}",
            userDetails.getUsername());

        userService.updateUserProfile(dto, file,  userDetails.getUsername());
        log.info("профиль пользователя: {} успешно изменен", userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("me/password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid UserChangePasswordDto dto,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/users/me/password - запрос на смену пароля пользователя: {}",
                userDetails.getUsername());
        userService.updateUserPassword(dto, userDetails.getUsername());
        log.info("пароль пользователя: {} успешно изменен", userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@RequestParam Long id) {
        log.info("POST /api/users/id - запрос на удаление пользователя с id: {}", id);
        userService.deleteUser(id);
        log.info("Пользователь с id: {} успешно удален", id);
        return ResponseEntity.ok().build();
    }

}

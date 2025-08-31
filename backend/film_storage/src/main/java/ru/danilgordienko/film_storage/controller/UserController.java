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
import ru.danilgordienko.film_storage.DTO.PageDto;
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
        log.info("GET /api/users/{id}/info - Request for user info with id={}", id);
        UserInfoDto userInfoDto = userService.getUserInfo(id);
        log.info("GET /api/users/{id}/info - User '{}' found", userInfoDto.getUsername());
        return ResponseEntity.ok(userInfoDto);
    }

    // получение страницы с пользователями
    @GetMapping
    public ResponseEntity<PageDto<UserListDto>> getAllUsers(@RequestParam(defaultValue = "0") int page) {
        log.info("GET /api/users/ - Request for fetching users page {}", page);
        var response = userService.getAllUsers(page);
        log.info("GET /api/users/ - Users page found, count of users {}", response.getContent().size());
        return ResponseEntity.ok(response);
    }

    // получение инфо пользователя текущего пользователя
    @GetMapping("/me/info")
    public ResponseEntity<UserInfoDto> getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/users/me/info - Request for current user info: {}", userDetails.getUsername());
        UserInfoDto userInfoDto = userService.getUserInfoByUsername(userDetails.getUsername());
        log.info("GET /api/users/me/info - Successfully receive current user info: {}", userDetails.getUsername());
        return ResponseEntity.ok(userInfoDto);
    }

    // поиск пользователя по запросу query
    @GetMapping("/search")
    public ResponseEntity<PageDto<UserListDto>> searchUsers(
            @RequestParam("query") String query,
            @RequestParam("page") int page) {
        log.info("GET /api/users/search - Search users request by query: '{}'", query);
        PageDto<UserListDto> users = userService.searchUserByUsername(query, page);
//        if (users.getContent().isEmpty()) {
//            log.warn("GET /api/users/search - No users found for query '{}'", query);
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        }
        log.info("GET /api/users/search - Found {} users for query '{}'", users.getContent().size(), query);
        return ResponseEntity.ok(users);
    }

    @PostMapping("me/profile/update")
    public ResponseEntity<String> updataProfile(
            @ModelAttribute @Valid UserProfileUpdateDto dto,
            @RequestParam(value = "avatar", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/users/me/profile/update - Request to update profile for user {}", userDetails.getUsername());
        userService.updateUserProfile(dto, file, userDetails.getUsername());
        log.info("POST /api/users/me/profile/update - Profile updated successfully for user {}", userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("me/password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid UserChangePasswordDto dto,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/users/me/password - Request to change password for user {}", userDetails.getUsername());
        userService.updateUserPassword(dto, userDetails.getUsername());
        log.info("POST /api/users/me/password - Password changed successfully for user {}", userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@RequestParam Long id) {
        log.info("POST /api/users/{id} - Request to delete user with id={}", id);
        userService.deleteUser(id);
        log.info("POST /api/users/{id} - User with id={} deleted successfully", id);
        return ResponseEntity.ok().build();
    }

}

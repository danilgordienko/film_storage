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
import ru.danilgordienko.film_storage.DTO.UsersDto.UserListDto;
import ru.danilgordienko.film_storage.service.FriendshipService;
import ru.danilgordienko.film_storage.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/friends")
@Slf4j
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final UserService userService;

    // получение друзей пользователя по id
    @GetMapping("/users/{id}")
    public ResponseEntity<UserFriendsDto> getUserFriends(@PathVariable Long id) {
        log.info("Запрос друзей пользователя с id: {}", id);

        UserFriendsDto friendsDto = userService.getUserFriends(id);
        log.info("Пользователь найден, всего друзей: {}", friendsDto.getFriends().size());

        return ResponseEntity.ok(friendsDto);
    }

    // получение друзей текущего пользователя
    @GetMapping
    public ResponseEntity<UserFriendsDto> getCurrentUserFriends(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос друзей текущего пользователя: {}", userDetails.getUsername());
        UserFriendsDto friends = friendshipService.getCurrentUserFriends(userDetails.getUsername());
        log.info("Друзья пользователя {} получены", userDetails.getUsername());
        return ResponseEntity.ok(friends);
    }

    // Отправить заявку в друзья
    @PostMapping("/request/{targetId}")
    public ResponseEntity<Void> sendFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable Long targetId) {
        log.info("Запрос от пользователя {} на отправку заявки в друзья пользователю с id: {}", userDetails.getUsername(), targetId);
        friendshipService.sendFriendRequest(userDetails.getUsername(), targetId);
        log.info("Пользователь {} успешно отправил заявку пользователю с id: {}", userDetails.getUsername(), targetId);
        return ResponseEntity.ok().build();
    }

    // Принять заявку в друзья
    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<Void> acceptFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long requesterId) {
        log.info("Запрос пользователя {} на принятие заявки от пользователя с id: {}", userDetails.getUsername(), requesterId);
        friendshipService.acceptFriendRequest(userDetails.getUsername(), requesterId);
        log.info("Пользователь {} успешно принял заявку от пользователя с id: {}", userDetails.getUsername(), requesterId);
        return ResponseEntity.ok().build();
    }

    // Отклонить заявку в друзья
    @PostMapping("/decline/{requesterId}")
    public ResponseEntity<Void> declineFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable Long requesterId) {
        log.info("Запрос пользователя {} на отклонение заявки от пользователя с id: {}", userDetails.getUsername(), requesterId);
        friendshipService.declineFriendRequest(userDetails.getUsername(), requesterId);
        log.info("Пользователь {} успешно отклонил заявку от пользователя с id: {}", userDetails.getUsername(), requesterId);
        return ResponseEntity.ok().build();
    }

    // Удалить из друзей
    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long friendId) {
        log.info("Запрос пользователя {} на удаление из друзей пользователя с id: {}", userDetails.getUsername(), friendId);
        friendshipService.removeFriend(userDetails.getUsername(), friendId);
        log.info("Пользователь {} успешно удалил из друзей пользователя с id: {}", userDetails.getUsername(), friendId);
        return ResponseEntity.ok().build();
    }

    // Посмотреть входящие заявки
    @GetMapping("/requests/incoming")
    public ResponseEntity<List<UserListDto>> getIncomingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Пользователь {} запрашивает входящие заявки", userDetails.getUsername());
        List<UserListDto> requests = friendshipService.getIncomingRequests(userDetails.getUsername());
        log.info("Входящие заявки для пользователя {} получены, всего:  {}", userDetails.getUsername(), requests.size());
        return ResponseEntity.ok(requests);
    }

    // Посмотреть исходящие заявки
    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<UserListDto>> getOutgoingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Пользователь {} запрашивает исходящие заявки", userDetails.getUsername());
        List<UserListDto> requests = friendshipService.getOutgoingRequests(userDetails.getUsername());
        log.info("Исходящие заявки для пользователя {} получены, всего:  {}", userDetails.getUsername(), requests.size());
        return ResponseEntity.ok(requests);
    }

}

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
        log.info("[GET /api/friends/users/{}] Request to get friends of user with id={}", id, id);
        UserFriendsDto friendsDto = userService.getUserFriends(id);
        log.info("[GET /api/friends/users/{}] User found, total friends={}", id, friendsDto.getFriends().size());
        return ResponseEntity.ok(friendsDto);
    }

    // получение друзей текущего пользователя
    @GetMapping
    public ResponseEntity<UserFriendsDto> getCurrentUserFriends(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("[GET /api/friends] Request to get current user's friends: {}", userDetails.getUsername());
        UserFriendsDto friends = friendshipService.getCurrentUserFriends(userDetails.getUsername());
        log.info("[GET /api/friends] Friends successfully retrieved for user={}", userDetails.getUsername());
        return ResponseEntity.ok(friends);
    }

    // Отправить заявку в друзья
    @PostMapping("/request/{targetId}")
    public ResponseEntity<Void> sendFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable Long targetId) {
        log.info("[POST /api/friends/request/{}] User {} sends friend request to user with id={}", targetId, userDetails.getUsername(), targetId);
        friendshipService.sendFriendRequest(userDetails.getUsername(), targetId);
        log.info("[POST /api/friends/request/{}] User {} successfully sent friend request", targetId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // Принять заявку в друзья
    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<Void> acceptFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long requesterId) {
        log.info("[POST /api/friends/accept/{}] User {} accepts friend request from user with id={}", requesterId, userDetails.getUsername(), requesterId);
        friendshipService.acceptFriendRequest(userDetails.getUsername(), requesterId);
        log.info("[POST /api/friends/accept/{}] User {} successfully accepted friend request", requesterId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // Отклонить заявку в друзья
    @PostMapping("/decline/{requesterId}")
    public ResponseEntity<Void> declineFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable Long requesterId) {
        log.info("[POST /api/friends/decline/{}] User {} declines friend request from user with id={}", requesterId, userDetails.getUsername(), requesterId);
        friendshipService.declineFriendRequest(userDetails.getUsername(), requesterId);
        log.info("[POST /api/friends/decline/{}] User {} successfully declined friend request", requesterId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // Удалить из друзей
    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long friendId) {
        log.info("[DELETE /api/friends/remove/{}] User {} removes friend with id={}", friendId, userDetails.getUsername(), friendId);
        friendshipService.removeFriend(userDetails.getUsername(), friendId);
        log.info("[DELETE /api/friends/remove/{}] User {} successfully removed friend", friendId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // Посмотреть входящие заявки
    @GetMapping("/requests/incoming")
    public ResponseEntity<List<UserListDto>> getIncomingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("[GET /api/friends/requests/incoming] User {} requests incoming friend requests", userDetails.getUsername());
        List<UserListDto> requests = friendshipService.getIncomingRequests(userDetails.getUsername());
        log.info("[GET /api/friends/requests/incoming] Incoming requests retrieved for user={}, total={}", userDetails.getUsername(), requests.size());
        return ResponseEntity.ok(requests);
    }

    // Посмотреть исходящие заявки
    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<UserListDto>> getOutgoingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("[GET /api/friends/requests/outgoing] User {} requests outgoing friend requests", userDetails.getUsername());
        List<UserListDto> requests = friendshipService.getOutgoingRequests(userDetails.getUsername());
        log.info("[GET /api/friends/requests/outgoing] Outgoing requests retrieved for user={}, total={}", userDetails.getUsername(), requests.size());
        return ResponseEntity.ok(requests);
    }

}

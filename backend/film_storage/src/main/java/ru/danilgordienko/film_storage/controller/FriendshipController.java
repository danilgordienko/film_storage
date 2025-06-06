package ru.danilgordienko.film_storage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.film_storage.DTO.FriendRequestDto;
import ru.danilgordienko.film_storage.DTO.UserFriendsDto;
import ru.danilgordienko.film_storage.service.FriendshipService;
import ru.danilgordienko.film_storage.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/friends")
@Slf4j
public class FriendshipController {
    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<UserFriendsDto> getCurrentUserFriends(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос друзей текущего пользователя: {}", userDetails.getUsername());

        return friendshipService.getCurrentUserFriends(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Друзья пользователя с username: {} не найдены", userDetails.getUsername());
                    return ResponseEntity.notFound().build();
                });
    }

    // Отправить заявку в друзья
    @PostMapping("/request/{targetId}")
    public ResponseEntity<Void> sendFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable Long targetId) {
        log.info("Запрос от пользователя {} на отправку заявки в друзья пользователю с id: {}", userDetails.getUsername(), targetId);

        if(!friendshipService.sendFriendRequest(userDetails.getUsername(), targetId)){
            log.warn("Ошибка при отправке заявки пользователя {} в друзья пользователю с id: {}", userDetails.getUsername(), targetId);
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    // Принять заявку в друзья
    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<Void> acceptFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long requesterId) {
        log.info("Запрос пользователя {} на принятие заявки от пользователя с id: {}", userDetails.getUsername(), requesterId);
        if(!friendshipService.acceptFriendRequest(userDetails.getUsername(), requesterId)){
            log.warn("Не удалось принять заявку пользователю {}  от пользователя с id: {}", userDetails.getUsername(), requesterId);
            return  ResponseEntity.badRequest().build();
        }
        log.info("Пользователь {} успешно принял заявку от пользователя с id: {}", userDetails.getUsername(), requesterId);
        return ResponseEntity.ok().build();
    }

    // Отклонить заявку в друзья
    @PostMapping("/decline/{requesterId}")
    public ResponseEntity<Void> declineFriendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable Long requesterId) {
        log.info("Запрос пользователя {} на отклонение заявки от пользователя с id: {}", userDetails.getUsername(), requesterId);
        if(!friendshipService.declineFriendRequest(userDetails.getUsername(), requesterId)){
            log.warn("Не удалось отклонить заявку пользователю {}  от пользователя с id: {}", userDetails.getUsername(), requesterId);
            return  ResponseEntity.badRequest().build();
        }
        log.info("Пользователь {} успешно отклонил заявку от пользователя с id: {}", userDetails.getUsername(), requesterId);
        return ResponseEntity.ok().build();
    }

    // Удалить из друзей
    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long friendId) {
        log.info("Запрос пользователя {} на удаление из друзей пользователя с id: {}", userDetails.getUsername(), friendId);
        if(!friendshipService.removeFriend(userDetails.getUsername(), friendId)){
            log.warn("Пользователю {} не удалось удалить из друзей пользователя с id: {}", userDetails.getUsername(), friendId);
            return  ResponseEntity.badRequest().build();
        }
        log.info("Пользователь {} успешно удалил из друзей пользователя с id: {}", userDetails.getUsername(), friendId);
        return ResponseEntity.ok().build();
    }

    // Посмотреть входящие заявки
    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendRequestDto>> getIncomingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Пользователь {} запрашивает входящие заявки", userDetails.getUsername());
        List<FriendRequestDto> requests = friendshipService.getIncomingRequests(userDetails.getUsername());
        return ResponseEntity.ok(requests);
    }

    // Посмотреть исходящие заявки
    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendRequestDto>> getOutgoingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Пользователь {} запрашивает исходящие заявки", userDetails.getUsername());
        List<FriendRequestDto> requests = friendshipService.getOutgoingRequests(userDetails.getUsername());
        return ResponseEntity.ok(requests);
    }

}

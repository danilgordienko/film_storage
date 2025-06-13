package ru.danilgordienko.film_storage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.FriendRequestDto;
import ru.danilgordienko.film_storage.DTO.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.mapping.FriendRequestMapping;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.model.FriendRequest;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.FriendRequestRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class FriendshipService {

    private final UserRepository userRepository;
    private final UserMapping  userMapping;
    private final FriendRequestRepository friendRequestRepository;


    //получение друзей текущего пользователя
    public Optional<UserFriendsDto> getCurrentUserFriends(String username) {
        log.info("Получение пользователя {} из бд", username);
        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserFriendsDto(user);
                });
    }

    //отправка заявки в друзья от текущего пользователя к пользователю с targetId
    public boolean sendFriendRequest(String username, Long targetId) {

        var friendRequest = checkAndGetFriendRequest(username, targetId, true);
        if(friendRequest.isEmpty())
            return false;

        friendRequestRepository.save(friendRequest.get());
        log.info("Заявка в друзья от пользователя {} пользователю с id: {} успешно отправлена",  username, targetId);
        return true;

    }

    //принятие заявки в друзья от пользователю с requesterId
    @Transactional
    public boolean acceptFriendRequest(String username, Long requesterId) {
        var friendRequest = checkAndGetFriendRequest(username, requesterId, false);
        if(friendRequest.isEmpty())
            return false;
        User sender = friendRequest.get().getSender();
        User receiver = friendRequest.get().getReceiver();
        var request = friendRequestRepository.findBySenderAndReceiver(sender, receiver);

        if (request.isEmpty()){
            return false;
        }
        // Удаляем запрос
        friendRequestRepository.delete(request.get());

        // Cохраняем друга с обеих сторон
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);
        userRepository.save(sender);
        userRepository.save(receiver);
        return true;
    }

    // вспомогательный метод для проверки существования пользователей
    // взависимости от того кто отправитель заявки строит FriendRequest
    private Optional<FriendRequest> checkAndGetFriendRequest(String username,
                                                             Long id,
                                                             boolean isUserWithUsernameSender) {
        var userWithUsername = userRepository.findByUsername(username);
        var userWithId = userRepository.findById(id);

        if (userWithId.isEmpty()) {
            log.warn("Пользователь c id: '{}' не найден", id);
            return Optional.empty();
        }
        if (userWithUsername.isEmpty()){
            log.warn("Пользователь '{}' не найден", username);
            return Optional.empty();
        }
        if (userWithUsername.get().equals(userWithId.get())) {
            log.warn("Отправитель и получатель совпадают: '{}' ", username);
            return Optional.empty();
        }
        // если первый юзер отправитель, то ставим его в sender
        if (isUserWithUsernameSender)
            return Optional.of(FriendRequest.builder()
                    .sender(userWithUsername.get())
                    .receiver(userWithId.get())
                    .build());
        // в противном случае ставим второго в sender
        return Optional.of(FriendRequest.builder()
                .receiver(userWithUsername.get())
                .sender(userWithId.get())
                .build());
    }

    //отклонение заявки в друзья от пользователю с requesterId
    public boolean declineFriendRequest(String username, Long requesterId) {
        var friendRequest = checkAndGetFriendRequest(username, requesterId, false);
        if(friendRequest.isEmpty())
            return false;
        User sender = friendRequest.get().getSender();
        User receiver = friendRequest.get().getReceiver();
        var request = friendRequestRepository.findBySenderAndReceiver(sender, receiver);

        if (request.isEmpty()){
            return false;
        }
        // Удаляем запрос
        friendRequestRepository.delete(request.get());
        return true;
    }

    // удаление пользователя из друзей
    @Transactional
    public boolean removeFriend(String username, Long friendId) {
        var friendRequest = checkAndGetFriendRequest(username, friendId, true);
        if(friendRequest.isEmpty())
            return false;
        User sender = friendRequest.get().getSender();
        User receiver = friendRequest.get().getReceiver();

        // удаляем с обеих сторон
        sender.getFriends().remove(receiver);
        receiver.getFriends().remove(sender);
        userRepository.save(sender);
        userRepository.save(receiver);
        return true;
    }

    // получение входящих запросов в друзья для текущего пользователя
    public List<UserInfoDto> getIncomingRequests(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            log.warn("");
            return List.of();
        }
        return friendRequestRepository.findByReceiver(user.get())
                .stream().map(r -> userMapping.toUserInfoDto(r.getSender())).toList();
    }

    // получение отправленных запросов от текущего пользователя
    public List<UserInfoDto> getOutgoingRequests(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            log.warn("");
            return List.of();
        }
        return friendRequestRepository.findBySender(user.get())
                .stream().map(r -> userMapping.toUserInfoDto(r.getReceiver())).toList();
    }


}

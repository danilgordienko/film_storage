package ru.danilgordienko.film_storage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.model.FriendRequest;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.FriendRequestRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;

import java.util.List;
import java.util.Optional;


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

    //получение друзей пользователя по id
    public Optional<UserFriendsDto> getUserFriends(Long id){
        log.info("Получение пользователя из бд с ID = {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserFriendsDto(user);
                });
    }

    //отправка заявки в друзья от текущего пользователя к пользователю с targetId
    public boolean sendFriendRequest(String username, Long targetId) {
        var sender = getUserByUsername(username);
        var reciever = getUserById(targetId);

        if (sender.isEmpty() || reciever.isEmpty()) {
            return false;
        }
        if (sender.equals(reciever)) {
            log.warn("Отправитель и получатель совпадают: '{}' ", username);
            return false;
        }
        friendRequestRepository.save(FriendRequest.builder()
                .sender(sender.get())
                .receiver(reciever.get())
                .build());
        log.info("Заявка в друзья от пользователя {} пользователю с id: {} успешно отправлена",  username, targetId);
        return true;

    }

    //принятие заявки в друзья от пользователю с requesterId
    @Transactional
    public boolean acceptFriendRequest(String username, Long requesterId) {
        var reciever = getUserByUsername(username);
        var sender = getUserById(requesterId);

        if (sender.isEmpty() || reciever.isEmpty()) {
            return false;
        }
        var request = friendRequestRepository.findBySenderAndReceiver(sender.get(), reciever.get());

        if (request.isEmpty()){
            return false;
        }
        // Удаляем запрос
        friendRequestRepository.delete(request.get());

        // Cохраняем друга с обеих сторон
        sender.get().getFriends().add(reciever.get());
        reciever.get().getFriends().add(sender.get());
        userRepository.save(sender.get());
        userRepository.save(reciever.get());
        return true;
    }

    private Optional<User> getUserByUsername(String username) {
        var userWithUsername = userRepository.findByUsername(username);
        if (userWithUsername.isEmpty()){
            log.warn("Пользователь '{}' не найден", username);
            return Optional.empty();
        }
        return userWithUsername;
    }

    private Optional<User> getUserById(Long id) {
        var user = userRepository.findById(id);
        if (user.isEmpty()){
            log.warn("Пользователь c '{}' не найден", id);
            return Optional.empty();
        }
        return user;
    }

    //отклонение заявки в друзья от пользователю с requesterId
    public boolean declineFriendRequest(String username, Long requesterId) {
        var reciever = getUserByUsername(username);
        var sender = getUserById(requesterId);

        if (sender.isEmpty() || reciever.isEmpty()) {
            return false;
        }
        var request = friendRequestRepository.findBySenderAndReceiver(sender.get(), reciever.get());

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
        var sender = getUserByUsername(username);
        var reciever = getUserById(friendId);

        if (sender.isEmpty() || reciever.isEmpty()) {
            return false;
        }

        // удаляем с обеих сторон
        sender.get().getFriends().remove(reciever.get());
        reciever.get().getFriends().remove(sender.get());
        userRepository.save(sender.get());
        userRepository.save(reciever.get());
        return true;
    }

    // получение входящих запросов в друзья для текущего пользователя
    public List<UserInfoDto> getIncomingRequests(String username) {
        return getUserByUsername(username)
                .map(user -> friendRequestRepository.findByReceiver(user)
                        .stream().map(r -> userMapping.toUserInfoDto(r.getSender())).toList())
                .orElseGet(List::of); //если пользователь не найден то возвращаем пустой список
    }

    // получение отправленных запросов от текущего пользователя
    public List<UserInfoDto> getOutgoingRequests(String username) {
        return getUserByUsername(username)
                .map(user -> friendRequestRepository.findBySender(user)
                        .stream().map(r -> userMapping.toUserInfoDto(r.getReceiver())).toList())
                .orElseGet(List::of); //если пользователь не найден то возвращаем пустой список
    }


}

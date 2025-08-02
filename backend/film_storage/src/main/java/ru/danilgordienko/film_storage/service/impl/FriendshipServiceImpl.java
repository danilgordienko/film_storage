package ru.danilgordienko.film_storage.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.FriendRequestNotFoundException;
import ru.danilgordienko.film_storage.exception.FriendshipAlreadyExistsException;
import ru.danilgordienko.film_storage.exception.FriendshipNotFoundException;
import ru.danilgordienko.film_storage.model.FriendRequest;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.FriendRequestRepository;
import ru.danilgordienko.film_storage.service.FriendshipService;
import ru.danilgordienko.film_storage.service.UserService;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final UserMapping  userMapping;
    private final FriendRequestRepository friendRequestRepository;
    private final UserService userService;


    //получение друзей текущего пользователя
    public UserFriendsDto getCurrentUserFriends(String username) {
        User user = userService.getUserByUsername(username);
        return userMapping.toUserFriendsDto(user);
    }

    //получение друзей пользователя по id
    public UserFriendsDto getUserFriends(Long id){
            User user = userService.getUserById(id);
            return userMapping.toUserFriendsDto(user);
    }

    //отправка заявки в друзья от текущего пользователя к пользователю с targetId
    @Transactional
    public void sendFriendRequest(String username, Long targetId) {
        try {
            var sender = userService.getUserByUsername(username);
            var reciever = userService.getUserById(targetId);

            if (sender.equals(reciever)) {
                log.warn("Отправитель и получатель совпадают: '{}' ", username);
                throw new IllegalStateException("Отправитель и получатель совпадают: " + "username");
            }

            if (sender.getFriends().contains(reciever)) {
                log.warn("Отправитель {} и получатель {} уже друзья", username, reciever.getUsername());
                throw new FriendshipAlreadyExistsException("Пользователь уже у вас в друзьях");
            }

            friendRequestRepository.save(FriendRequest.builder()
                    .sender(sender)
                    .receiver(reciever)
                    .build());
            log.info("Заявка в друзья от пользователя {} пользователю с id: {} успешно отправлена", username, targetId);
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }

    }

    //принятие заявки в друзья от пользователю с requesterId
    @Transactional
    public void acceptFriendRequest(String username, Long requesterId) {
        try {
            var sender = userService.getUserByUsername(username);
            var reciever = userService.getUserById(requesterId);

            var request = friendRequestRepository.findBySenderAndReceiver(sender, reciever);

            if (request.isEmpty()) {
                log.warn("Попытка принять не существующий запрос от {} к {}",  reciever.getUsername(), username);
                throw new FriendRequestNotFoundException("Заявки в друзья не существует");
            }
            // Удаляем запрос
            friendRequestRepository.delete(request.get());

            // Cохраняем друга с обеих сторон
            sender.getFriends().add(reciever);
            reciever.getFriends().add(sender);
            userService.saveUser(sender);
            userService.saveUser(reciever);
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    //отклонение заявки в друзья от пользователю с requesterId
    @Transactional
    public void declineFriendRequest(String username, Long requesterId) {
        try {
            var sender = userService.getUserByUsername(username);
            var reciever = userService.getUserById(requesterId);

            var request = friendRequestRepository.findBySenderAndReceiver(sender, reciever);

            if (request.isEmpty()) {
                log.warn("Попытка отклонить не существующий запрос от {} к {}",  reciever.getUsername(), username);
                throw new FriendRequestNotFoundException("Заявки в друзья не существует");
            }
            // Удаляем запрос
            friendRequestRepository.delete(request.get());
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    // удаление пользователя из друзей
    @Transactional
    public void removeFriend(String username, Long friendId) {
        try {
            var sender = userService.getUserByUsername(username);
            var reciever = userService.getUserById(friendId);

            if (!sender.getFriends().contains(reciever)) {
                log.warn("Пользователь {} пытался удалить не существующего друга {}", username, reciever.getUsername());
                throw new FriendshipNotFoundException("");
            }

            // удаляем с обеих сторон
            sender.getFriends().remove(reciever);
            reciever.getFriends().remove(sender);
            userService.saveUser(sender);
            userService.saveUser(reciever);
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    // получение входящих запросов в друзья для текущего пользователя
    public List<UserInfoDto> getIncomingRequests(String username) {
        try {
            User user = userService.getUserByUsername(username);
            return friendRequestRepository.findByReceiver(user)
                    .stream().map(r -> userMapping.toUserInfoDto(r.getSender())).toList();
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    // получение отправленных запросов от текущего пользователя
    public List<UserInfoDto> getOutgoingRequests(String username) {
        try {
            User user = userService.getUserByUsername(username);
            return friendRequestRepository.findBySender(user)
                    .stream().map(r -> userMapping.toUserInfoDto(r.getReceiver())).toList();
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }


}

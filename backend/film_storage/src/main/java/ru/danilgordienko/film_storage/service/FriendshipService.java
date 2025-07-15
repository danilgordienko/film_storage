package ru.danilgordienko.film_storage.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.MappingException;
import org.springframework.dao.DataAccessException;
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

    private final UserMapping  userMapping;
    private final FriendRequestRepository friendRequestRepository;
    private final UserService userService;


    //получение друзей текущего пользователя
    public Optional<UserFriendsDto> getCurrentUserFriends(String username) {
        try {
            User user = userService.getUserByUsername(username);
            return Optional.of(userMapping.toUserFriendsDto(user));
        } catch (DataAccessException | EntityNotFoundException e ) {
            return Optional.empty();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //получение друзей пользователя по id
    public Optional<UserFriendsDto> getUserFriends(Long id){
        try {
            User user = userService.getUserById(id);
            return Optional.of(userMapping.toUserFriendsDto(user));
        } catch (DataAccessException | EntityNotFoundException e ) {
            return Optional.empty();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //отправка заявки в друзья от текущего пользователя к пользователю с targetId
    public boolean sendFriendRequest(String username, Long targetId) {
        try {
            var sender = userService.getUserByUsername(username);
            var reciever = userService.getUserById(targetId);

            if (sender.equals(reciever)) {
                log.warn("Отправитель и получатель совпадают: '{}' ", username);
                return false;
            }
            friendRequestRepository.save(FriendRequest.builder()
                    .sender(sender)
                    .receiver(reciever)
                    .build());
            log.info("Заявка в друзья от пользователя {} пользователю с id: {} успешно отправлена", username, targetId);
            return true;
        } catch (DataAccessException | EntityNotFoundException e ) {
            return false;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            return false;
        }

    }

    //принятие заявки в друзья от пользователю с requesterId
    @Transactional
    public boolean acceptFriendRequest(String username, Long requesterId) {
        var sender = userService.getUserByUsername(username);
        var reciever = userService.getUserById(requesterId);

        var request = friendRequestRepository.findBySenderAndReceiver(sender, reciever);

        if (request.isEmpty()){
            return false;
        }
        // Удаляем запрос
        friendRequestRepository.delete(request.get());

        // Cохраняем друга с обеих сторон
        sender.getFriends().add(reciever);
        reciever.getFriends().add(sender);
        userService.saveUser(sender);
        userService.saveUser(reciever);
        return true;
    }

    //отклонение заявки в друзья от пользователю с requesterId
    @Transactional
    public boolean declineFriendRequest(String username, Long requesterId) {
        try {
            var sender = userService.getUserByUsername(username);
            var reciever = userService.getUserById(requesterId);

            var request = friendRequestRepository.findBySenderAndReceiver(sender, reciever);

            if (request.isEmpty()) {
                return false;
            }
            // Удаляем запрос
            friendRequestRepository.delete(request.get());
            return true;
        } catch (DataAccessException | EntityNotFoundException e ) {
            return false;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            return false;
        }
    }

    // удаление пользователя из друзей
    @Transactional
    public boolean removeFriend(String username, Long friendId) {
        var sender = userService.getUserByUsername(username);
        var reciever = userService.getUserById(friendId);

        // удаляем с обеих сторон
        sender.getFriends().remove(reciever);
        reciever.getFriends().remove(sender);
        userService.saveUser(sender);
        userService.saveUser(reciever);
        return true;
    }

    // получение входящих запросов в друзья для текущего пользователя
    public List<UserInfoDto> getIncomingRequests(String username) {
        try {
            User user = userService.getUserByUsername(username);
            return friendRequestRepository.findByReceiver(user)
                    .stream().map(r -> userMapping.toUserInfoDto(r.getSender())).toList();
        }catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return List.of();
        } catch (DataAccessException | EntityNotFoundException e ) {
            return List.of();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // получение отправленных запросов от текущего пользователя
    public List<UserInfoDto> getOutgoingRequests(String username) {
        try {
            User user = userService.getUserByUsername(username);
            return friendRequestRepository.findBySender(user)
                    .stream().map(r -> userMapping.toUserInfoDto(r.getReceiver())).toList();
        }catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return List.of();
        } catch (DataAccessException | EntityNotFoundException e ) {
            return List.of();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            return List.of();
        }
    }


}

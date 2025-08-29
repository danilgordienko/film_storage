package ru.danilgordienko.film_storage.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserListDto;
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

    private final UserMapping userMapping;
    private final FriendRequestRepository friendRequestRepository;
    private final UserService userService;

    // Get friends of the current user
    public UserFriendsDto getCurrentUserFriends(String username) {
        User user = userService.getUserByEmail(username);
        log.debug("Retrieving friends for current user '{}'", username);
        return userMapping.toUserFriendsDto(user);
    }

    // Get friends of a user by id
    public UserFriendsDto getUserFriends(Long id) {
        User user = userService.getUserById(id);
        log.debug("Retrieving friends for user with ID '{}'", id);
        return userMapping.toUserFriendsDto(user);
    }

    // Send friend request from current user to targetId
    @Transactional
    public void sendFriendRequest(String username, Long targetId) {
        try {
            var sender = userService.getUserByEmail(username);
            var receiver = userService.getUserById(targetId);

            if (sender.equals(receiver)) {
                log.warn("Sender and receiver are the same: '{}'", username);
                throw new IllegalStateException("Sender and receiver are the same: " + username);
            }

            if (sender.getFriends().contains(receiver)) {
                log.warn("Sender '{}' and receiver '{}' are already friends", username, receiver.getUsername());
                throw new FriendshipAlreadyExistsException("User is already in your friends list");
            }

            friendRequestRepository.save(FriendRequest.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .build());
            log.debug("Friend request successfully sent from '{}' to user ID '{}'", username, targetId);
        } catch (DataAccessException e) {
            log.error("Database access error while sending friend request", e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Accept friend request from requesterId
    @Transactional
    public void acceptFriendRequest(String username, Long requesterId) {
        try {
            var sender = userService.getUserByEmail(username);
            var receiver = userService.getUserById(requesterId);

            var request = friendRequestRepository.findBySenderAndReceiver(sender, receiver);

            if (request.isEmpty()) {
                log.warn("Attempt to accept non-existing friend request from '{}' to '{}'", receiver.getUsername(), username);
                throw new FriendRequestNotFoundException("Friend request does not exist");
            }

            // Remove request
            friendRequestRepository.delete(request.get());

            // Save friends on both sides
            sender.getFriends().add(receiver);
            receiver.getFriends().add(sender);
            userService.saveUser(sender);
            userService.saveUser(receiver);
            log.debug("Friend request accepted between '{}' and '{}'", username, receiver.getUsername());
        } catch (DataAccessException e) {
            log.error("Database access error while accepting friend request", e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Decline friend request from requesterId
    @Transactional
    public void declineFriendRequest(String username, Long requesterId) {
        try {
            var sender = userService.getUserByEmail(username);
            var receiver = userService.getUserById(requesterId);

            var request = friendRequestRepository.findBySenderAndReceiver(sender, receiver);

            if (request.isEmpty()) {
                log.warn("Attempt to decline non-existing friend request from '{}' to '{}'", receiver.getUsername(), username);
                throw new FriendRequestNotFoundException("Friend request does not exist");
            }

            // Remove request
            friendRequestRepository.delete(request.get());
            log.debug("Friend request declined from '{}' to '{}'", receiver.getUsername(), username);
        } catch (DataAccessException e) {
            log.error("Database access error while declining friend request", e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Remove friend
    @Transactional
    public void removeFriend(String username, Long friendId) {
        try {
            var sender = userService.getUserByEmail(username);
            var receiver = userService.getUserById(friendId);

            if (!sender.getFriends().contains(receiver)) {
                log.warn("User '{}' attempted to remove non-existing friend '{}'", username, receiver.getUsername());
                throw new FriendshipNotFoundException("Friendship does not exist");
            }

            // Remove friendship from both sides
            sender.getFriends().remove(receiver);
            receiver.getFriends().remove(sender);
            userService.saveUser(sender);
            userService.saveUser(receiver);
            log.debug("Friend '{}' removed from user '{}''s friends list", receiver.getUsername(), username);
        } catch (DataAccessException e) {
            log.error("Database access error while removing friend", e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Get incoming friend requests for current user
    public List<UserListDto> getIncomingRequests(String username) {
        try {
            User user = userService.getUserByEmail(username);
            log.debug("Retrieving incoming friend requests for user '{}'", username);
            return friendRequestRepository.findByReceiver(user)
                    .stream().map(r -> userMapping.toUserListDto(r.getSender())).toList();
        } catch (DataAccessException e) {
            log.error("Database access error while retrieving incoming requests", e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Get outgoing friend requests from current user
    public List<UserListDto> getOutgoingRequests(String username) {
        try {
            User user = userService.getUserByEmail(username);
            log.debug("Retrieving outgoing friend requests for user '{}'", username);
            return friendRequestRepository.findBySender(user)
                    .stream().map(r -> userMapping.toUserListDto(r.getReceiver())).toList();
        } catch (DataAccessException e) {
            log.error("Database access error while retrieving outgoing requests", e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }
}

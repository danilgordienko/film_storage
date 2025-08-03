package ru.danilgordienko.film_storage.service;

import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserListDto;

import java.util.List;

@Service
public interface FriendshipService {

    UserFriendsDto getCurrentUserFriends(String username);
    UserFriendsDto getUserFriends(Long id);
    void sendFriendRequest(String username, Long targetId);
    void acceptFriendRequest(String username, Long requesterId);
    void declineFriendRequest(String username, Long requesterId);
    void removeFriend(String username, Long friendId);
    List<UserListDto> getIncomingRequests(String username);
    List<UserListDto> getOutgoingRequests(String username);
}

package ru.danilgordienko.film_storage.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.danilgordienko.film_storage.DTO.UsersDto.*;
import ru.danilgordienko.film_storage.model.User;

import java.util.List;

@Service
public interface UserService extends UserDetailsService {

    User getUserByUsername(String username);
    User getUserById(Long id);
    List<UserListDto> searchUserByUsername(String query);
    void saveUser(User user);
    UserInfoDto getUserInfo(Long id);
    UserFriendsDto getUserFriends(Long id);
    UserInfoDto getUserInfoByUsername(String username);
    void deleteUser(Long id);
    void updateUserProfile(UserProfileUpdateDto userProfileUpdateDto,
                    MultipartFile profileImage,
                    String username);
    void updateUserPassword(UserChangePasswordDto  userChangePasswordDto,
                            String username);
}

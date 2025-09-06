package ru.danilgordienko.film_storage.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.danilgordienko.film_storage.model.dto.PageDto;
import ru.danilgordienko.film_storage.model.dto.UsersDto.*;
import ru.danilgordienko.film_storage.model.entity.User;
import ru.danilgordienko.film_storage.model.enums.RatingVisibility;

@Service
public interface UserService extends UserDetailsService {

    User getUserByUsername(String username);
    User getUserById(Long id);
    PageDto<UserListDto> searchUserByUsername(String query, int page);
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
    User getUserByEmail(String email);
    PageDto<UserListDto> getAllUsers(int page);
    void setRatingVisibility(RatingVisibility ratingVisibility, String username);

    UserSettingsDto getSettings(String username);
}

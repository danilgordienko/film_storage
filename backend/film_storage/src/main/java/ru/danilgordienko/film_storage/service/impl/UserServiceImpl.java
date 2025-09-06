package ru.danilgordienko.film_storage.service.impl;


import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import ru.danilgordienko.film_storage.model.dto.PageDto;
import ru.danilgordienko.film_storage.model.dto.UsersDto.*;
import ru.danilgordienko.film_storage.model.dto.mapping.UserMapping;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.ElasticsearchConnectionException;
import ru.danilgordienko.film_storage.exception.UserNotFoundException;
import ru.danilgordienko.film_storage.exception.UserUpdateException;
import ru.danilgordienko.film_storage.model.entity.User;
import ru.danilgordienko.film_storage.model.enums.RatingVisibility;
import ru.danilgordienko.film_storage.repository.UserRepository;
import ru.danilgordienko.film_storage.repository.UserSearchRepository;
import ru.danilgordienko.film_storage.security.UserDetailsImpl;
import ru.danilgordienko.film_storage.service.UserService;

import java.io.IOException;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapping userMapping;
    private final UserSearchRepository userSearchRepository;
    private final PasswordEncoder passwordEncoder;
    private final int size = 20;

    //загрузка пользователей по username. нужен для spring security для авторизации пользователя
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Searching for user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found", email);
                    return new UsernameNotFoundException(email + " not found");
                });

        log.debug("User '{}' found, returning UserDetails", email);
        return UserDetailsImpl.build(user);
    }

    //получение пользователей по username.
    @Override
    public User getUserByUsername(String username) {
        try {
            log.debug("Searching for user by username: {}", username);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("User '{}' not found", username);
                        return new UserNotFoundException("User with username " + username + " not found");
                    });
        } catch (DataAccessException e) {
            log.error("Database access error: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Failed to connect to the database", e);
        }
    }

    //получение пользователей по username.
    @Override
    public User getUserByEmail(String email) {
        try {
            log.debug("Searching for user by email: {}", email);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("User '{}' not found", email);
                        return new UserNotFoundException("User with email " + email + " not found");
                    });
        } catch (DataAccessException e) {
            log.error("Database access error: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Failed to connect to the database", e);
        }
    }

    public PageDto<UserListDto> getAllUsers(int page) {
        log.debug("Fetching users page {}", page);
        Pageable pageable = PageRequest.of(page, size);
        var users = userRepository.findAll(pageable);
        return userMapping.toUserListDtoPage(users);
    }

    //получение пользователей по id.
    @Override
    public User getUserById(Long id) {
        try {
            log.debug("Fetching user from DB with ID = {}", id);
            return userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("User with id '{}' not found", id);
                        return new UserNotFoundException("User with id " + id + " not found");
                    });
        } catch (DataAccessException e) {
            log.error("Database access error while fetching by ID", e);
            throw new DatabaseConnectionException("Failed to fetch user from DB", e);
        }
    }

    // поиск пользователя по имени из Elasticsearch
    @Override
    public PageDto<UserListDto> searchUserByUsername(String query, int page){
        try {
            log.debug("Searching users in Elasticsearch by username: {}", query);
            Pageable pageable = PageRequest.of(page, size);
            var searchResults = userSearchRepository.searchByUsernameContaining(query,  pageable);
            return userMapping.toUserListDtoPageFromUserDocument(searchResults);
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Elasticsearch access error", e);
            throw new ElasticsearchConnectionException("Failed to search user in Elasticsearch", e);
        }
    }

    @Transactional
    public void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataAccessException e) {
            log.error("Database save error", e);
            throw new DatabaseConnectionException("Failed to save user in DB", e);
        }
    }

    // получение информации о пользователе
    @Override
    public UserInfoDto getUserInfo(Long id) {
        User user = getUserById(id);
        return userMapping.toUserInfoDto(user);
    }

    // получение друзей пользователя
    @Override
    public UserFriendsDto getUserFriends(Long id) {
        User user = getUserById(id);
        return userMapping.toUserFriendsDto(user);
    }

    // получение информации о пользователе по его email
    @Override
    public UserInfoDto getUserInfoByUsername(String username){
        User user = getUserByEmail(username);
        return userMapping.toUserInfoDto(user);
    }

    //Удаление пользователя по id(только для админов)
    @Override
    @Transactional
    public void deleteUser(Long id) {
        if(userRepository.existsById(id)){
            userRepository.deleteById(id);
            userSearchRepository.deleteById(id);
            return;
        }
        throw new UserNotFoundException("User with id " + id + " does not exist");
    }

    @Override
    @Transactional
    public void updateUserProfile(UserProfileUpdateDto userProfileUpdateDto,
                                  MultipartFile avatar,
                                  String username) {
        try {
            User user = getUserByEmail(username);
            System.out.println("DTO class: " + userProfileUpdateDto.getClass());

            if (userProfileUpdateDto.getEmail() != null)
                user.setEmail(userProfileUpdateDto.getEmail());
            if (userProfileUpdateDto.getUsername() != null)
                user.setUsername(userProfileUpdateDto.getUsername());
            if (avatar != null && !avatar.isEmpty()) {
                user.setAvatar(avatar.getBytes());
            }
            userRepository.save(user);
        } catch (IOException e) {
            throw new UserUpdateException("Error reading file");
        }
    }

    @Override
    @Transactional
    public void updateUserPassword(UserChangePasswordDto userChangePasswordDto, String username) {
        try {
            if (!userChangePasswordDto.getNewPassword().equals(userChangePasswordDto.getNewPasswordConfirm())) {
                throw new UserUpdateException("Passwords do not match");
            }

            final User savedUser = getUserByEmail(username);

            if (!passwordEncoder.matches(userChangePasswordDto.getOldPassword(),
                    savedUser.getPassword())) {
                throw new UserUpdateException("Incorrect password");
            }

            final String encodedPassword = passwordEncoder.encode(userChangePasswordDto.getNewPassword());
            savedUser.setPassword(encodedPassword);
            userRepository.save(savedUser);
        } catch (DataAccessException e) {
            log.error("Database save error", e);
            throw new DatabaseConnectionException("Failed to save user in DB", e);
        }
    }

    @Override
    @Transactional
    public void setRatingVisibility(RatingVisibility ratingVisibility, String username) {
        User user = getUserByEmail(username);
        user.setRatingVisibility(ratingVisibility);
    }

    @Override
    public UserSettingsDto getSettings(String username) {
        User user = getUserByEmail(username);
        return userMapping.toUserSettingDto(user);
    }
}


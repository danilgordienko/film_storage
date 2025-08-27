package ru.danilgordienko.film_storage.service.impl;


import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import ru.danilgordienko.film_storage.DTO.UsersDto.*;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.ElasticsearchConnectionException;
import ru.danilgordienko.film_storage.exception.UserNotFoundException;
import ru.danilgordienko.film_storage.exception.UserUpdateException;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.UserRepository;
import ru.danilgordienko.film_storage.repository.UserSearchRepository;
import ru.danilgordienko.film_storage.security.UserDetailsImpl;
import ru.danilgordienko.film_storage.service.UserService;

import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapping userMapping;
    private final UserSearchRepository  userSearchRepository;
    private final PasswordEncoder passwordEncoder;

    //загрузка пользователей по username. нужен для spring security для авторизации пользователя
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Поиск пользователя по email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Пользователь '{}' не найден", email);
                    return new UsernameNotFoundException(email + " not found");
                });

        log.info("Пользователь '{}' найден, возвращаем UserDetails", email);
        return UserDetailsImpl.build(user);
    }

    //получение пользователей по username.
    @Override
    public User getUserByUsername(String username) {
        try {
            log.info("Поиск пользователя по имени: {}", username);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Пользователь '{}' не найден", username);
                        return new UserNotFoundException("Пользователь с именем " + username + " не найден");
                    });
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    //получение пользователей по username.
    @Override
    public User getUserByEmail(String email) {
        try {
            log.info("Поиск пользователя по email: {}", email);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Пользователь '{}' не найден", email);
                        return new UserNotFoundException("Пользователь с email " + email + " не найден");
                    });
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    //получение пользователей по id.
    @Override
    public User getUserById(Long id) {
        try {
            log.info("Получение пользователя из бд с ID = {}", id);
            return userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Пользователь c id '{}' не найден", id);
                        return new UserNotFoundException("Пользователь c id " + id + " не найден");
                    });
        } catch (DataAccessException e) {
            log.error("Ошибка подключения к БД при поиске по ID", e);
            throw new DatabaseConnectionException("Не удалось получить пользователя из БД", e);
        }
    }

    // поиск пользователя по имени из Elasticsearch
    @Override
    public List<UserListDto> searchUserByUsername(String query){
        try {
            log.info("Поиск пользователей в Elasticsearch по имени: {}", query);

            var searchResults = userSearchRepository.searchByUsernameContaining(query);
            var users = searchResults.stream()
                    .map(user -> {
                        var u = getUserById(user.getId());
                        return userMapping.toUserListDto(u);
                    }).toList();

            log.info("Найдено {} пользователей в Elasticsearch по запросу '{}'", users.size(), query);

            return users;
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Ошибка при работе с Elasticsearch", e);
            throw new ElasticsearchConnectionException("Не удалось найти пользователя в Elasticsearch", e);
        }
    }

    @Transactional
    public void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataAccessException e) {
            log.error("Ошибка сохранения в БД", e);
            throw new DatabaseConnectionException("Не удалось сохранить пользователя в БД", e);
        }
    }

//    // проверяет зарегистрирован ли уже пользователь
//    public boolean existsUser(String username) {
//        try {
//            boolean exists = userRepository.existsByUsername(username);
//            log.info("Проверка существования пользователя '{}': {}", username, exists);
//            return exists;
//        } catch (DataAccessException e) {
//            log.error("Ошибка сохранения в БД", e);
//            throw new DatabaseConnectionException("Не удалось сохранить пользователя в БД", e);
//        }
//    }

    // получение информации о пользователе
    public UserInfoDto getUserInfo(Long id) {
        User user = getUserById(id);
        return userMapping.toUserInfoDto(user);
    }


    // получение друзей пользователя
    public UserFriendsDto getUserFriends(Long id) {
        User user = getUserById(id);
        return userMapping.toUserFriendsDto(user);
    }


    // получение информации о пользователе по его email
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
        throw new UserNotFoundException("Пользователя с id " + id + "не существует");
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
            throw new UserUpdateException("ошибка при чтении файла");
        }
    }

    @Override
    @Transactional
    public void updateUserPassword(UserChangePasswordDto userChangePasswordDto, String username) {
        try {
            if (!userChangePasswordDto.getNewPassword().equals(userChangePasswordDto.getNewPasswordConfirm())) {
                throw new UserUpdateException("пароли не совпадают");
            }

            final User savedUser = getUserByEmail(username);

            if (!passwordEncoder.matches(userChangePasswordDto.getOldPassword(),
                    savedUser.getPassword())) {
                throw new UserUpdateException("неверный пароль");
            }

            final String encodedPassword = passwordEncoder.encode(userChangePasswordDto.getNewPassword());
            savedUser.setPassword(encodedPassword);
            userRepository.save(savedUser);
        } catch (DataAccessException e) {
            log.error("Ошибка сохранения в БД", e);
            throw new DatabaseConnectionException("Не удалось сохранить пользователя в БД", e);
        }
    }


}

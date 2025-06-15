package ru.danilgordienko.film_storage.service;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRegistrationDTO;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.UserRepository;
import ru.danilgordienko.film_storage.repository.UserSearchRepository;
import ru.danilgordienko.film_storage.security.UserDetailsImpl;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private UserMapping userMapping;
    private UserSearchRepository  userSearchRepository;

    //загрузка пользователей по username. нужен для spring security для авторизации пользователя
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Поиск пользователя по имени: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь '{}' не найден", username);
                    return new UsernameNotFoundException(username + " not found");
                });

        log.info("Пользователь '{}' найден, возвращаем UserDetails", username);
        return UserDetailsImpl.build(user);
    }

    // поиск пользователя по имени из Elasticsearch
    public List<UserInfoDto> searchUserByUsername(String query){
        log.info("Поиск пользователей в Elasticsearch по имени: {}", query);

        var searchResults = userSearchRepository.searchByUsername(query);

        var users = searchResults.stream()
                .map(userMapping::toUserInfoDto)
                .toList();

        log.info("Найдено {} пользователей в Elasticsearch по запросу '{}'", users.size(), query);

        return users;

    }

    // регистрация пользователя
    @Transactional
    public void addUser(UserRegistrationDTO user) {
        log.info("Регистрация нового пользователя: {}", user.getUsername());

        User userToAdd = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();

        userRepository.save(userToAdd);
        // так же добавляем в elasticsearch
        userSearchRepository.save(userMapping.toUserDocument(userToAdd));
        log.info("Пользователь '{}' успешно зарегистрирован", user.getUsername());
    }

    // проверяет зарегистрирован ли уже пользователь
    public Boolean existsUser(String username) {
        boolean exists = userRepository.existsByUsername(username);
        log.info("Проверка существования пользователя '{}': {}", username, exists);
        return exists;
    }

    // получение информации о пользователе
    public Optional<UserInfoDto> getUserInfo(Long id){
        log.info("Получение пользователя из бд с ID = {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserInfoDto(user);
                });
    }

    // получение друзей пользователя
    public Optional<UserFriendsDto> getUserFriends(Long id){
        log.info("Получение пользователя из бд с ID = {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserFriendsDto(user);
                });
    }

    // получение информации о пользователе по его username
    public Optional<UserInfoDto> getUserInfoByUsername(String username){
        log.info("Получение пользователя {} из бд", username);
        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserInfoDto(user);
                });
    }


}

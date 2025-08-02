package ru.danilgordienko.film_storage.service;


import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.MappingException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRegistrationDTO;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.ElasticsearchConnectionException;
import ru.danilgordienko.film_storage.exception.UserNotFoundException;
import ru.danilgordienko.film_storage.exception.UserRegistrationException;
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

    //получение пользователей по username.
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

    //получение пользователей по id.
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
    public List<UserInfoDto> searchUserByUsername(String query){
        try {
            log.info("Поиск пользователей в Elasticsearch по имени: {}", query);

            var searchResults = userSearchRepository.searchByUsername(query);

            var users = searchResults.stream()
                    .map(userMapping::toUserInfoDto)
                    .toList();

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


    // получение информации о пользователе по его username
    public UserInfoDto getUserInfoByUsername(String username){
        User user = getUserByUsername(username);
        return userMapping.toUserInfoDto(user);
    }



}

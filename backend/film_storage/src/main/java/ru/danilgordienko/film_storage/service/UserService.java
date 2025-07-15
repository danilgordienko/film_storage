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
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Пользователь '{}' не найден", username);
                        return new EntityNotFoundException(username + " not found");
                    });
            log.info("Пользователь {} найден",  username);
            return user;
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при получении пользователя: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении пользователя", e);
        }
    }

    //получение пользователей по username.
    public User getUserById(Long id) {
        try {
            log.info("Получение пользователя из бд с ID = {}", id);
            return userRepository.findById(id)
                    .map(user -> {
                        log.info("Пользователь найден: {}", user.getUsername());
                        return user;
                    }).orElseThrow(() -> {
                        log.warn("Пользователь c id '{}' не найден", id);
                        return new EntityNotFoundException(id + " not found");
                    });
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при получении пользователя: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении пользователя", e);
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
        } catch (DataAccessException | EntityNotFoundException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return List.of();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при получении пользователя: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // регистрация пользователя
    @Transactional
    public boolean addUser(UserRegistrationDTO user) {
        try {
            log.info("Регистрация нового пользователя: {}", user.getUsername());

            User userToAdd = User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .build();

            userRepository.save(userToAdd);
            // так же добавляем в elasticsearch
            userSearchRepository.save(userMapping.toUserDocument(userToAdd));
            log.info("Пользователь '{}' успешно зарегистрирован", user.getUsername());
            return true;
        } catch (DataAccessException | EntityNotFoundException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return false;
        }catch (ElasticsearchException | RestClientException e) {
            log.error("Ошибка подключения к Elasticsearch: {}", e.getMessage(), e);
            return false;
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при получении пользователя: {}", e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при добавлении пользователя: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при добавлении пользователя", e);
        }
    }

    // проверяет зарегистрирован ли уже пользователь
    public boolean existsUser(String username) {
        try {
            boolean exists = userRepository.existsByUsername(username);
            log.info("Проверка существования пользователя '{}': {}", username, exists);
            return exists;
        } catch (DataAccessException | EntityNotFoundException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при получении пользователя: {}", e.getMessage(), e);
            return false;
        }
    }

    // получение информации о пользователе
    public Optional<UserInfoDto> getUserInfo(Long id){
        try {
            User user = getUserById(id);
            return Optional.of(userMapping.toUserInfoDto(user));
        } catch (DataAccessException | EntityNotFoundException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return Optional.empty();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при получении пользователя: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // получение друзей пользователя
    public Optional<UserFriendsDto> getUserFriends(Long id){
        try {
            User user = getUserById(id);
            return Optional.of(userMapping.toUserFriendsDto(user));
        } catch (DataAccessException | EntityNotFoundException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return Optional.empty();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при получении пользователя: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // получение информации о пользователе по его username
    public Optional<UserInfoDto> getUserInfoByUsername(String username){
        try {
            User user = getUserByUsername(username);
            return Optional.of(userMapping.toUserInfoDto(user));
        } catch (DataAccessException | EntityNotFoundException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }


}

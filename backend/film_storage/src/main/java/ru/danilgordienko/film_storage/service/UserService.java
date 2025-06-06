package ru.danilgordienko.film_storage.service;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UserFriendsDto;
import ru.danilgordienko.film_storage.DTO.UserInfoDto;
import ru.danilgordienko.film_storage.DTO.UserRatingDto;
import ru.danilgordienko.film_storage.DTO.UserRegistrationDTO;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.UserRepository;
import ru.danilgordienko.film_storage.security.UserDetailsImpl;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private UserMapping userMapping;


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

    public void addUser(UserRegistrationDTO user) {
        log.info("Регистрация нового пользователя: {}", user.getUsername());

        User userToAdd = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();

        userRepository.save(userToAdd);
        log.info("Пользователь '{}' успешно зарегистрирован", user.getUsername());
    }

    public Boolean existsUser(String username) {
        boolean exists = userRepository.existsByUsername(username);
        log.info("Проверка существования пользователя '{}': {}", username, exists);
        return exists;
    }

    public Optional<UserInfoDto> getUserInfo(Long id){
        log.info("Получение пользователя из бд с ID = {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserInfoDto(user);
                });
    }

    public Optional<UserRatingDto> getUserRatings(Long id){
        log.info("Получение пользователя из бд с ID = {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserRatingDto(user);
                });
    }

    public Optional<UserFriendsDto> getUserFriends(Long id){
        log.info("Получение пользователя из бд с ID = {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserFriendsDto(user);
                });
    }

    public Optional<UserRatingDto> getUserRatingsByUsername(String username){
        log.info("Получение пользователя {} из бд", username);
        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserRatingDto(user);
                });
    }

    public Optional<UserInfoDto> getUserInfoByUsername(String username){
        log.info("Получение пользователя {} из бд", username);
        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserInfoDto(user);
                });
    }


}

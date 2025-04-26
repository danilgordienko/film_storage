package ru.danilgordienko.film_storage.service;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UserRegistrationDTO;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.UserRepository;
import ru.danilgordienko.film_storage.security.UserDetailsImpl;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
        return UserDetailsImpl.build(user) ;
    }

    public void addUser(UserRegistrationDTO user) {
        User userToAdd = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
        userRepository.save(userToAdd);
    }

    public Boolean existsUser(String username) {
        return userRepository.existsByUsername(username);
    }
}

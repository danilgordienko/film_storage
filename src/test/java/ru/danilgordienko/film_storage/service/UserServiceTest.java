package ru.danilgordienko.film_storage.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testAddUser() {

    }

    @Test
    public void testLoadUserByUsername() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("test");
        user.setId(1L);
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("test");

        assertNotNull(result);
        assertEquals("test", result.getUsername());
    }

    @Test
    public void testLoadUserByUsernameWhenNotFound() {
        when(userRepository.findByUsername("test")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("test");
        });
    }
}

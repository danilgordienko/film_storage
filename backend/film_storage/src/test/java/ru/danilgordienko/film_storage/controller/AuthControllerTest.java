package ru.danilgordienko.film_storage.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.danilgordienko.film_storage.model.entity.User;
import ru.danilgordienko.film_storage.security.JWTCore;
import ru.danilgordienko.film_storage.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTCore jwtCore;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLogin() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword("password");
        String userJson = objectMapper.writeValueAsString(user);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtCore.generateToken(authentication)).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("jwt-token"));
    }

    @Test
    void testLoginWithBadCredentials() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword("wrong");
        String userJson = objectMapper.writeValueAsString(user);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new UsernameNotFoundException("user not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLoginWhenUserNotFound() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword("wrong");
        String userJson = objectMapper.writeValueAsString(user);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterWhenUserAlreadyExists() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword("test1234");
        String userJson = objectMapper.writeValueAsString(user);

        when(userService.existsUser("test")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isConflict());
    }

    @Test
    void testRegister() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword("pass1234");
        String userJson = objectMapper.writeValueAsString(user);

        when(userService.existsUser("test")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded-pass");


        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());

        verify(userService).addUser(argThat(u ->
                u.getUsername().equals("test") &&
                u.getPassword().equals("encoded-pass")));

    }

    @Test
    void testRegisterWhenOtherExceptionThrown() throws Exception {
        User user = new User();
        user.setUsername("fail_user");
        user.setPassword("pass1234");
        String userJson = objectMapper.writeValueAsString(user);

        when(userService.existsUser("fail_user")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded-pass");
        doThrow(new RuntimeException("DB error")).when(userService).addUser(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }
}

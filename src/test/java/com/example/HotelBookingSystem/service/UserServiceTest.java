package com.example.HotelBookingSystem.service;

import com.example.HotelBookingSystem.entity.Role;
import com.example.HotelBookingSystem.entity.User;
import com.example.HotelBookingSystem.exception.UserAlreadyExistsException;
import com.example.HotelBookingSystem.repository.RoleRepository;
import com.example.HotelBookingSystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setFirstName("Kaviya");
        user.setEmail("kaviya@example.com");
        user.setPassword("plainPassword");

        role = new Role("ROLE_USER");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.registerUser(user);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(role));

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(user));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        List<User> users = userService.getUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("kaviya@example.com", users.get(0).getEmail());
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getEmail());

        verify(userRepository, times(1)).deleteByEmail(user.getEmail());
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.deleteUser(user.getEmail()));

        verify(userRepository, never()).deleteByEmail(anyString());
    }

    @Test
    void testGetUser_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User foundUser = userService.getUser(user.getEmail());

        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void testGetUser_NotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUser(user.getEmail()));
    }
}

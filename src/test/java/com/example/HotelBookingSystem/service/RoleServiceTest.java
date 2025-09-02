package com.example.HotelBookingSystem.service;

import com.example.HotelBookingSystem.entity.Role;
import com.example.HotelBookingSystem.entity.User;
import com.example.HotelBookingSystem.exception.RoleAlreadyExistException;
import com.example.HotelBookingSystem.exception.UserAlreadyExistsException;
import com.example.HotelBookingSystem.repository.RoleRepository;
import com.example.HotelBookingSystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        role = new Role("ROLE_ADMIN");
        role.setId(1L);

        user = new User();
        user.setId(1L);
        user.setFirstName("Kaviya");
    }

    @Test
    void testGetRoles() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role));

        List<Role> roles = roleService.getRoles();

        assertEquals(1, roles.size());
        assertEquals("ROLE_ADMIN", roles.get(0).getName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void testCreateRole_Success() {
        when(roleRepository.existsByName("ROLE_MANAGER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(new Role("ROLE_MANAGER"));

        Role savedRole = roleService.createRole(new Role("manager"));

        assertEquals("ROLE_MANAGER", savedRole.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testCreateRole_AlreadyExists() {
        when(roleRepository.existsByName("ROLE_ADMIN")).thenReturn(true);

        assertThrows(RoleAlreadyExistException.class,
                () -> roleService.createRole(new Role("admin")));
    }

    @Test
    void testFindByName_Found() {
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));

        Role found = roleService.findByName("ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", found.getName());
    }

    @Test
    void testFindByName_NotFound() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> roleService.findByName("ROLE_USER"));
    }

    @Test
    void testAssignRoleToUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        User updatedUser = roleService.assignRoleToUser(1L, 1L);

        assertEquals(user.getId(), updatedUser.getId());
        verify(roleRepository).save(role);
    }

    @Test
    void testAssignRoleToUser_AlreadyAssigned() {
        user.getRoles().add(role);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        assertThrows(UserAlreadyExistsException.class,
                () -> roleService.assignRoleToUser(1L, 1L));
    }

    @Test
    void testRemoveUserFromRole_Success() {
        role.assignRoleToUser(user); // manually add user
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        User removedUser = roleService.removeUserFromRole(1L, 1L);

        assertEquals(user.getId(), removedUser.getId());
        verify(roleRepository).save(role);
    }

    @Test
    void testRemoveUserFromRole_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        assertThrows(UsernameNotFoundException.class,
                () -> roleService.removeUserFromRole(1L, 1L));
    }

    @Test
    void testRemoveAllUserFromRole_Success() {
        role.assignRoleToUser(user);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role updatedRole = roleService.removeAllUserFromRole(1L);

        assertTrue(updatedRole.getUsers().isEmpty());
        verify(roleRepository).save(role);
    }
}

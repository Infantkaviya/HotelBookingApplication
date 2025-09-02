package com.example.HotelBookingSystem.controller;

import com.example.HotelBookingSystem.entity.Role;
import com.example.HotelBookingSystem.entity.User;
import com.example.HotelBookingSystem.exception.RoleAlreadyExistException;
import com.example.HotelBookingSystem.service.IRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IRoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllRoles() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        Mockito.when(roleService.getRoles()).thenReturn(Collections.singletonList(role));

        mockMvc.perform(get("/roles/all-roles"))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void testCreateRole_Success() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        Mockito.doNothing().when(roleService).createRole(any(Role.class));

        mockMvc.perform(post("/roles/create-new-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk())
                .andExpect(content().string("New role created successfully!"));
    }

    @Test
    void testCreateRole_AlreadyExists() throws Exception {
        Role role = new Role();
        role.setName("ADMIN");

        Mockito.doThrow(new RoleAlreadyExistException("Role already exists"))
                .when(roleService).createRole(any(Role.class));

        mockMvc.perform(post("/roles/create-new-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Role already exists"));
    }

    @Test
    void testDeleteRole() throws Exception {
        Mockito.doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/roles/delete/{roleId}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testRemoveAllUsersFromRole() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        Mockito.when(roleService.removeAllUserFromRole(1L)).thenReturn(role);

        mockMvc.perform(post("/roles/remove-all-users-from-role/{roleId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("USER"));
    }

    @Test
    void testRemoveUserFromRole() throws Exception {
        User user = new User();
        user.setId(10L);
        user.setEmail("test@example.com");

        Mockito.when(roleService.removeUserFromRole(10L, 1L)).thenReturn(user);

        mockMvc.perform(post("/roles/remove-user-from-role")
                        .param("userId", "10")
                        .param("roleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testAssignUserToRole() throws Exception {
        User user = new User();
        user.setId(5L);
        user.setEmail("newuser@example.com");

        Mockito.when(roleService.assignRoleToUser(5L, 1L)).thenReturn(user);

        mockMvc.perform(post("/roles/assign-user-to-role")
                        .param("userId", "5")
                        .param("roleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }
}

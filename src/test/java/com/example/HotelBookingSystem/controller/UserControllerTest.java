package com.example.HotelBookingSystem.controller;

import com.example.HotelBookingSystem.entity.User;
import com.example.HotelBookingSystem.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetUsers() throws Exception {
        User u1 = new User();
        u1.setId(1L);
        u1.setEmail("a@example.com");

        User u2 = new User();
        u2.setId(2L);
        u2.setEmail("b@example.com");

        List<User> users = Arrays.asList(u1, u2);

        Mockito.when(userService.getUsers()).thenReturn(users);

        mockMvc.perform(get("/users/all"))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$[0].email").value("a@example.com"))
                .andExpect(jsonPath("$[1].email").value("b@example.com"));
    }

    @Test
    void testGetUserById_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Mockito.when(userService.getUser("1")).thenReturn(user);

        mockMvc.perform(get("/users/profile/{userId}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        Mockito.when(userService.getUser("99"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/users/profile/{userId}", "99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void testGetUserById_InternalError() throws Exception {
        Mockito.when(userService.getUser("2"))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/users/profile/{userId}", "2"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error fetching user"));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        Mockito.doNothing().when(userService).deleteUser("a@example.com");

        mockMvc.perform(delete("/users/delete/{userId}", "a@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        Mockito.doThrow(new UsernameNotFoundException("User not found"))
                .when(userService).deleteUser("notfound@example.com");

        mockMvc.perform(delete("/users/delete/{userId}", "notfound@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void testDeleteUser_InternalError() throws Exception {
        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(userService).deleteUser("error@example.com");

        mockMvc.perform(delete("/users/delete/{userId}", "error@example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error deleting user: Unexpected error"));
    }
}

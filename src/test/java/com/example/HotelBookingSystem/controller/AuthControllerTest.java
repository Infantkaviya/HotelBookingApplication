package com.example.HotelBookingSystem.controller;

import com.example.HotelBookingSystem.entity.User;
import com.example.HotelBookingSystem.exception.UserAlreadyExistsException;
import com.example.HotelBookingSystem.request.LoginRequest;
import com.example.HotelBookingSystem.response.JwtResponse;
import com.example.HotelBookingSystem.security.jwt.JwtUtils;
import com.example.HotelBookingSystem.security.user.HotelUserDetails;
import com.example.HotelBookingSystem.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private LoginRequest loginRequest;
    private HotelUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        userDetails = new HotelUserDetails(
                1L,
                "test@example.com",
                "password",
                List.of(() -> "ROLE_USER")
        );
    }

    @Test
    void testRegisterUserSuccess() throws Exception {
        Mockito.doNothing().when(userService).registerUser(any(User.class));

        mockMvc.perform(post("/auth/register-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful!"));
    }

    @Test
    void testRegisterUserAlreadyExists() throws Exception {
        Mockito.doThrow(new UserAlreadyExistsException("User already exists"))
                .when(userService).registerUser(any(User.class));

        mockMvc.perform(post("/auth/register-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(content().string("User already exists"));
    }

    @Test
    void testAuthenticateUser() throws Exception {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        Mockito.when(jwtUtils.generateJwtTokenForUser(authentication))
                .thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }
}

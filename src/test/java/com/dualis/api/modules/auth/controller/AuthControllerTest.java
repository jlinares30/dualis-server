package com.dualis.api.modules.auth.controller;

import com.dualis.api.modules.auth.application.usecase.ManageAuthUseCase;
import com.dualis.api.modules.auth.dto.request.LoginRequest;
import com.dualis.api.modules.auth.dto.request.RegisterRequest;
import com.dualis.api.modules.auth.dto.response.AuthResponse;
import com.dualis.api.modules.auth.dto.response.UserProfileResponse;
import com.dualis.api.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageAuthUseCase authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .accessToken("jwt.token.here")
                .tokenType("Bearer")
                .userId(UUID.randomUUID())
                .email("jorge@example.com")
                .firstName("Jorge")
                .lastName("Linares")
                .role("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Success")
    void register_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("jorge@example.com")
                .password("Password123!")
                .firstName("Jorge")
                .lastName("Linares")
                .baseCurrency("USD")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt.token.here"))
                .andExpect(jsonPath("$.email").value("jorge@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Success")
    void login_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("jorge@example.com")
                .password("Password123!")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt.token.here"));
    }
}

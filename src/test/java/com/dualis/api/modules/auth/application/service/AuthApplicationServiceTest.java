package com.dualis.api.modules.auth.application.service;

import com.dualis.api.dto.request.LoginRequest;
import com.dualis.api.dto.request.RegisterRequest;
import com.dualis.api.dto.response.AuthResponse;
import com.dualis.api.modules.auth.domain.model.User;
import com.dualis.api.modules.auth.domain.model.UserRole;
import com.dualis.api.modules.auth.domain.repository.UserRepositoryPort;
import com.dualis.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthApplicationService authApplicationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .passwordHash("encoded_pwd")
                .firstName("John")
                .lastName("Doe")
                .baseCurrency("USD")
                .role(UserRole.ROLE_USER)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("user@example.com")
                .password("securePassword123")
                .firstName("John")
                .lastName("Doe")
                .baseCurrency("USD")
                .build();

        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("encoded_pwd");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(user.getEmail(), "ROLE_USER")).thenReturn("jwt_token_123");

        AuthResponse response = authApplicationService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt_token_123");
        assertThat(response.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Should login user successfully")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("securePassword123")
                .build();

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("securePassword123", "encoded_pwd")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getEmail(), "ROLE_USER")).thenReturn("jwt_token_123");

        AuthResponse response = authApplicationService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt_token_123");
    }
}

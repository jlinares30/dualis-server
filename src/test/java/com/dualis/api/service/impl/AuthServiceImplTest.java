package com.dualis.api.service.impl;

import com.dualis.api.domain.model.User;
import com.dualis.api.domain.model.UserRole;
import com.dualis.api.domain.repository.UserRepository;
import com.dualis.api.dto.request.LoginRequest;
import com.dualis.api.dto.request.RegisterRequest;
import com.dualis.api.dto.response.AuthResponse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("jorge@example.com")
                .passwordHash("hashedPassword")
                .firstName("Jorge")
                .lastName("Linares")
                .baseCurrency("USD")
                .role(UserRole.ROLE_USER)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should register new user and return JWT token")
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("jorge@example.com")
                .password("Secret123!")
                .firstName("Jorge")
                .lastName("Linares")
                .baseCurrency("USD")
                .build();

        when(userRepository.existsByEmailIgnoreCase("jorge@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(eq("jorge@example.com"), any())).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt.token.here");
        assertThat(response.getEmail()).isEqualTo("jorge@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when registering duplicate email")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("jorge@example.com")
                .build();

        when(userRepository.existsByEmailIgnoreCase("jorge@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("jorge@example.com")
                .password("Secret123!")
                .build();

        when(userRepository.findByEmailIgnoreCase("jorge@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("jorge@example.com", "ROLE_USER")).thenReturn("jwt.token.here");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt.token.here");
    }
}

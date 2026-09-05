package com.dualis.api.modules.auth.application.service;

import com.dualis.api.domain.model.UserRole;
import com.dualis.api.dto.request.LoginRequest;
import com.dualis.api.dto.request.RegisterRequest;
import com.dualis.api.dto.request.UpdateProfileRequest;
import com.dualis.api.dto.response.AuthResponse;
import com.dualis.api.dto.response.UserProfileResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.auth.application.usecase.ManageAuthUseCase;
import com.dualis.api.modules.auth.domain.model.User;
import com.dualis.api.modules.auth.domain.repository.UserRepositoryPort;
import com.dualis.api.security.JwtTokenProvider;
import com.dualis.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements ManageAuthUseCase, AuthService {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("An account with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .baseCurrency(request.getBaseCurrency() != null ? request.getBaseCurrency() : "USD")
                .role(com.dualis.api.modules.auth.domain.model.UserRole.ROLE_USER)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = findUserByEmail(email);
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);
        user.updateProfile(request.getFirstName(), request.getLastName(), request.getBaseCurrency());
        User updated = userRepository.save(user);
        return mapToProfileResponse(updated);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .baseCurrency(user.getBaseCurrency())
                .role(user.getRole() != null ? user.getRole().name() : "ROLE_USER")
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

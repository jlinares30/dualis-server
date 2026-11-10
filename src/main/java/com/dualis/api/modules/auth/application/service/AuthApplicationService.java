package com.dualis.api.modules.auth.application.service;

import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.auth.application.usecase.ManageAuthUseCase;
import com.dualis.api.modules.auth.domain.model.User;
import com.dualis.api.modules.auth.domain.model.UserRole;
import com.dualis.api.modules.auth.domain.repository.UserRepositoryPort;
import com.dualis.api.modules.auth.dto.request.LoginRequest;
import com.dualis.api.modules.auth.dto.request.RegisterRequest;
import com.dualis.api.modules.auth.dto.request.UpdateProfileRequest;
import com.dualis.api.modules.auth.dto.response.AuthResponse;
import com.dualis.api.modules.auth.dto.response.UserProfileResponse;
import com.dualis.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.dualis.api.modules.account.application.usecase.ManageAccountUseCase;
import com.dualis.api.modules.account.domain.model.AccountType;
import com.dualis.api.modules.account.dto.request.CreateAccountRequest;
import com.dualis.api.modules.workspace.application.usecase.ManageWorkspaceUseCase;
import com.dualis.api.modules.workspace.dto.request.CreateWorkspaceRequest;
import com.dualis.api.modules.workspace.dto.response.WorkspaceResponse;
import com.dualis.api.shared.domain.valueobject.Money;
import com.dualis.api.modules.auth.dto.request.OnboardingRequest;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements ManageAuthUseCase {

    private final UserRepositoryPort userRepository;
    private final ManageWorkspaceUseCase workspaceUseCase;
    private final ManageAccountUseCase accountUseCase;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttempts;

    /** A session can be renewed while active, but never beyond this many days after login. */
    @org.springframework.beans.factory.annotation.Value("${app.jwt.max-session-days:30}")
    private long maxSessionDays = 30;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new IllegalArgumentException("An account with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .baseCurrency(request.getBaseCurrency() != null ? request.getBaseCurrency().trim().toUpperCase() : Money.DEFAULT_CURRENCY)
                .role(UserRole.ROLE_USER)
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
                .onboardingCompleted(savedUser.isOnboardingCompleted())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        loginAttempts.checkAllowed(email);

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttempts.recordFailure(email);
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is disabled");
        }
        loginAttempts.recordSuccess(email);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .onboardingCompleted(user.isOnboardingCompleted())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserProfileResponse.fromDomain(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.updateProfile(request.getFirstName(), request.getLastName(), request.getBaseCurrency());
        User updated = userRepository.save(user);

        return UserProfileResponse.fromDomain(updated);
    }

    @Override
    @Transactional
    public UserProfileResponse completeOnboarding(String email, OnboardingRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // 1. Update preferred currency if provided
        String currency = (request.getBaseCurrency() != null && !request.getBaseCurrency().isBlank())
                ? request.getBaseCurrency().trim().toUpperCase()
                : (user.getBaseCurrency() != null ? user.getBaseCurrency() : Money.DEFAULT_CURRENCY);

        user.updateProfile(user.getFirstName(), user.getLastName(), currency);
        user.completeOnboarding();
        User updatedUser = userRepository.save(user);

        // 2. Ensure the requested workspace exists. Workspaces and accounts are created through their
        //    own modules' use cases (the owner is always the authenticated user).
        boolean wantsCouple = "COUPLE".equalsIgnoreCase(request.getWorkspaceMode());
        WorkspaceType targetType = wantsCouple ? WorkspaceType.COUPLE : WorkspaceType.INDIVIDUAL;
        UUID targetWorkspaceId = workspaceUseCase.getCurrentUserWorkspaces().stream()
                .filter(w -> w.getType() == targetType)
                .map(WorkspaceResponse::getId)
                .findFirst()
                .orElseGet(() -> workspaceUseCase.createWorkspace(CreateWorkspaceRequest.builder()
                        .name(wantsCouple ? "Espacio en Pareja" : "Espacio Personal")
                        .description(wantsCouple ? "Finanzas compartidas" : "Finanzas personales")
                        .type(targetType)
                        .currency(currency)
                        .build()).getId());
        // A partner is never added from here: they must accept by joining with the invitation code
        // of the couple workspace (request.partnerEmail is ignored).

        // 3. Create initial financial account if specified
        if (request.getAccountName() != null && !request.getAccountName().isBlank()) {
            AccountType accountType;
            try {
                accountType = AccountType.valueOf(request.getAccountType().trim().toUpperCase());
            } catch (Exception e) {
                accountType = AccountType.SAVINGS;
            }
            accountUseCase.createAccount(CreateAccountRequest.builder()
                    .workspaceId(targetWorkspaceId)
                    .name(request.getAccountName().trim())
                    .type(accountType)
                    .balance(request.getInitialBalance() != null ? request.getInitialBalance() : java.math.BigDecimal.ZERO)
                    .currency(currency)
                    .description("Cuenta inicial configurada en el onboarding")
                    .isIncludedInTotal(true)
                    .build());
        }

        return UserProfileResponse.fromDomain(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String email, String currentToken) {
        java.util.Date sessionStart = jwtTokenProvider.getSessionStart(currentToken);
        long sessionAgeMs = System.currentTimeMillis() - sessionStart.getTime();
        if (sessionAgeMs > java.time.Duration.ofDays(maxSessionDays).toMillis()) {
            throw new org.springframework.security.authentication.CredentialsExpiredException(
                    "Session expired, please log in again");
        }
        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElseThrow(() -> new org.springframework.security.authentication.DisabledException("Account is not active"));

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name(), sessionStart);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .onboardingCompleted(user.isOnboardingCompleted())
                .build();
    }
}

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

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.model.AccountType;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.auth.dto.request.OnboardingRequest;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.model.WorkspaceRole;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements ManageAuthUseCase {

    private final UserRepositoryPort userRepository;
    private final WorkspaceRepositoryPort workspaceRepository;
    private final AccountRepositoryPort accountRepository;
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
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is disabled");
        }

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
                : (user.getBaseCurrency() != null ? user.getBaseCurrency() : "USD");

        user.updateProfile(user.getFirstName(), user.getLastName(), currency);
        user.completeOnboarding();
        User updatedUser = userRepository.save(user);

        // 2. Ensure individual or couple workspace exists for the user
        List<Workspace> userWorkspaces = workspaceRepository.findWorkspacesByUserEmail(email);
        Workspace targetWorkspace = null;

        boolean wantsCouple = "COUPLE".equalsIgnoreCase(request.getWorkspaceMode());

        if (wantsCouple) {
            targetWorkspace = userWorkspaces.stream()
                    .filter(w -> w.getType() == WorkspaceType.COUPLE)
                    .findFirst()
                    .orElse(null);

            if (targetWorkspace == null) {
                targetWorkspace = Workspace.builder()
                        .name("Espacio en Pareja")
                        .description("Finanzas compartidas")
                        .type(WorkspaceType.COUPLE)
                        .currency(currency)
                        .ownerEmail(email)
                        .isActive(true)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();
                targetWorkspace.addMember(email, WorkspaceRole.OWNER);
                if (request.getPartnerEmail() != null && !request.getPartnerEmail().isBlank()) {
                    targetWorkspace.addMember(request.getPartnerEmail().trim().toLowerCase(), WorkspaceRole.PARTNER);
                }
                targetWorkspace = workspaceRepository.save(targetWorkspace);
            }
        } else {
            targetWorkspace = userWorkspaces.stream()
                    .filter(w -> w.getType() == WorkspaceType.INDIVIDUAL)
                    .findFirst()
                    .orElse(null);

            if (targetWorkspace == null) {
                targetWorkspace = Workspace.builder()
                        .name("Espacio Personal")
                        .description("Finanzas personales")
                        .type(WorkspaceType.INDIVIDUAL)
                        .currency(currency)
                        .ownerEmail(email)
                        .isActive(true)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();
                targetWorkspace.addMember(email, WorkspaceRole.OWNER);
                targetWorkspace = workspaceRepository.save(targetWorkspace);
            }
        }

        // 3. Create initial financial account if specified
        if (request.getAccountName() != null && !request.getAccountName().isBlank()) {
            AccountType accountType;
            try {
                accountType = AccountType.valueOf(request.getAccountType().trim().toUpperCase());
            } catch (Exception e) {
                accountType = AccountType.SAVINGS;
            }

            java.math.BigDecimal startBal = request.getInitialBalance() != null ? request.getInitialBalance() : java.math.BigDecimal.ZERO;

            Account initialAccount = Account.builder()
                    .workspaceId(targetWorkspace.getId())
                    .name(request.getAccountName().trim())
                    .type(accountType)
                    .balance(Money.of(startBal, currency))
                    .status(AccountStatus.ACTIVE)
                    .isIncludedInTotal(true)
                    .description("Cuenta inicial configurada en el onboarding")
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            accountRepository.save(initialAccount);
        }

        return UserProfileResponse.fromDomain(updatedUser);
    }
}

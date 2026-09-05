package com.dualis.api.modules.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class User {

    private final UUID id;
    private final String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String baseCurrency;
    private UserRole role;
    private boolean isActive;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void updateProfile(String firstName, String lastName, String baseCurrency) {
        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName.trim();
        }
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName.trim();
        }
        if (baseCurrency != null && !baseCurrency.isBlank()) {
            this.baseCurrency = baseCurrency.trim();
        }
        this.updatedAt = OffsetDateTime.now();
    }
}

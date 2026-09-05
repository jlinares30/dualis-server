package com.dualis.api.modules.auth.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.auth.domain.model.User;
import com.dualis.api.modules.auth.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .baseCurrency(entity.getBaseCurrency())
                .role(entity.getRole())
                .isActive(Boolean.TRUE.equals(entity.getIsActive()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;

        return UserJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .baseCurrency(domain.getBaseCurrency())
                .role(domain.getRole())
                .isActive(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

package com.dualis.api.modules.auth.domain.repository;

import com.dualis.api.modules.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    void delete(UUID id);
}

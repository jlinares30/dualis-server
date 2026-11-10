package com.dualis.api.modules.auth.application.service;

import com.dualis.api.modules.auth.application.usecase.UserDirectoryUseCase;
import com.dualis.api.modules.auth.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserDirectoryService implements UserDirectoryUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getDisplayNamesByEmail(Collection<String> emails) {
        Map<String, String> names = new HashMap<>();
        if (emails == null) {
            return names;
        }
        for (String email : emails) {
            if (email == null || email.isBlank()) continue;
            String key = email.trim().toLowerCase();
            if (names.containsKey(key)) continue;
            userRepository.findByEmailIgnoreCase(key).ifPresent(u -> {
                String full = ((u.getFirstName() != null ? u.getFirstName() : "") + " "
                        + (u.getLastName() != null ? u.getLastName() : "")).trim();
                if (!full.isBlank()) {
                    names.put(key, full);
                }
            });
        }
        return names;
    }
}

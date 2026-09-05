package com.dualis.api.modules.auth.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.auth.domain.model.User;
import com.dualis.api.modules.auth.domain.repository.UserRepositoryPort;
import com.dualis.api.modules.auth.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.dualis.api.modules.auth.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.dualis.api.modules.auth.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toEntity(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        return springDataUserRepository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return springDataUserRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public void delete(UUID id) {
        springDataUserRepository.deleteById(id);
    }
}

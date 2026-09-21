package com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.subscription.domain.model.Subscription;
import com.dualis.api.modules.subscription.domain.repository.SubscriptionRepositoryPort;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.entity.SubscriptionJpaEntity;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.mapper.SubscriptionPersistenceMapper;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.repository.SpringDataSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionPersistenceAdapter implements SubscriptionRepositoryPort {

    private final SpringDataSubscriptionRepository springDataSubscriptionRepository;
    private final SubscriptionPersistenceMapper mapper;

    public SubscriptionPersistenceAdapter(SpringDataSubscriptionRepository springDataSubscriptionRepository, SubscriptionPersistenceMapper mapper) {
        this.springDataSubscriptionRepository = springDataSubscriptionRepository;
        this.mapper = mapper;
    }

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionJpaEntity entity = mapper.toEntity(subscription);
        SubscriptionJpaEntity saved = springDataSubscriptionRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return springDataSubscriptionRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findByWorkspaceId(UUID workspaceId) {
        return springDataSubscriptionRepository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        springDataSubscriptionRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataSubscriptionRepository.existsById(id);
    }
}

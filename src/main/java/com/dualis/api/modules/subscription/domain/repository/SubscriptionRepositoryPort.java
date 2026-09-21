package com.dualis.api.modules.subscription.domain.repository;

import com.dualis.api.modules.subscription.domain.model.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepositoryPort {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(UUID id);
    List<Subscription> findByWorkspaceId(UUID workspaceId);
    void delete(UUID id);
    boolean existsById(UUID id);
}

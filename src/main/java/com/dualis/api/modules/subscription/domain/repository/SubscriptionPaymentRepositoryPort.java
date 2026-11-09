package com.dualis.api.modules.subscription.domain.repository;

import com.dualis.api.modules.subscription.domain.model.SubscriptionPayment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPaymentRepositoryPort {
    SubscriptionPayment save(SubscriptionPayment payment);
    Optional<SubscriptionPayment> findBySubscriptionIdAndPeriod(UUID subscriptionId, int year, int month);
    List<SubscriptionPayment> findBySubscriptionId(UUID subscriptionId);
    List<SubscriptionPayment> findByWorkspaceIdAndPeriod(UUID workspaceId, int year, int month);
    void delete(UUID id);
    void deleteBySubscriptionId(UUID subscriptionId);
}

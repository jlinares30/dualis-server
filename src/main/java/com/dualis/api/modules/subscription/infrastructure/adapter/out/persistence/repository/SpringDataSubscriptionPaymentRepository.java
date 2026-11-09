package com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.repository;

import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.entity.SubscriptionPaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSubscriptionPaymentRepository extends JpaRepository<SubscriptionPaymentJpaEntity, UUID> {
    Optional<SubscriptionPaymentJpaEntity> findBySubscriptionIdAndPeriodYearAndPeriodMonth(UUID subscriptionId, Integer year, Integer month);
    List<SubscriptionPaymentJpaEntity> findBySubscriptionIdOrderByPeriodYearDescPeriodMonthDesc(UUID subscriptionId);
    List<SubscriptionPaymentJpaEntity> findByWorkspaceIdAndPeriodYearAndPeriodMonth(UUID workspaceId, Integer year, Integer month);

    @Modifying
    void deleteBySubscriptionId(UUID subscriptionId);
}

package com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.subscription.domain.model.SubscriptionPayment;
import com.dualis.api.modules.subscription.domain.repository.SubscriptionPaymentRepositoryPort;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.entity.SubscriptionPaymentJpaEntity;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.repository.SpringDataSubscriptionPaymentRepository;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubscriptionPaymentPersistenceAdapter implements SubscriptionPaymentRepositoryPort {

    private final SpringDataSubscriptionPaymentRepository repository;

    @Override
    public SubscriptionPayment save(SubscriptionPayment payment) {
        return toDomain(repository.save(toEntity(payment)));
    }

    @Override
    public Optional<SubscriptionPayment> findBySubscriptionIdAndPeriod(UUID subscriptionId, int year, int month) {
        return repository.findBySubscriptionIdAndPeriodYearAndPeriodMonth(subscriptionId, year, month).map(this::toDomain);
    }

    @Override
    public List<SubscriptionPayment> findBySubscriptionId(UUID subscriptionId) {
        return repository.findBySubscriptionIdOrderByPeriodYearDescPeriodMonthDesc(subscriptionId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<SubscriptionPayment> findByWorkspaceIdAndPeriod(UUID workspaceId, int year, int month) {
        return repository.findByWorkspaceIdAndPeriodYearAndPeriodMonth(workspaceId, year, month).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteBySubscriptionId(UUID subscriptionId) {
        repository.deleteBySubscriptionId(subscriptionId);
    }

    private SubscriptionPayment toDomain(SubscriptionPaymentJpaEntity e) {
        return SubscriptionPayment.builder()
                .id(e.getId())
                .subscriptionId(e.getSubscriptionId())
                .workspaceId(e.getWorkspaceId())
                .periodYear(e.getPeriodYear())
                .periodMonth(e.getPeriodMonth())
                .amount(Money.of(e.getAmount(), e.getCurrency()))
                .paidAt(e.getPaidAt())
                .paidByEmail(e.getPaidByEmail())
                .transactionId(e.getTransactionId())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private SubscriptionPaymentJpaEntity toEntity(SubscriptionPayment p) {
        return SubscriptionPaymentJpaEntity.builder()
                .id(p.getId())
                .subscriptionId(p.getSubscriptionId())
                .workspaceId(p.getWorkspaceId())
                .periodYear(p.getPeriodYear())
                .periodMonth(p.getPeriodMonth())
                .amount(p.getAmount().amount())
                .currency(p.getAmount().currency())
                .paidAt(p.getPaidAt())
                .paidByEmail(p.getPaidByEmail())
                .transactionId(p.getTransactionId())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

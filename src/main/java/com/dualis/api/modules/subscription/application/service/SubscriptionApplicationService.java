package com.dualis.api.modules.subscription.application.service;

import com.dualis.api.dto.request.CreateSubscriptionRequest;
import com.dualis.api.dto.response.SubscriptionResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.subscription.application.usecase.ManageSubscriptionUseCase;
import com.dualis.api.modules.subscription.domain.model.Subscription;
import com.dualis.api.modules.subscription.domain.repository.SubscriptionRepositoryPort;
import com.dualis.api.service.SubscriptionService;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionApplicationService implements ManageSubscriptionUseCase, SubscriptionService {

    private final SubscriptionRepositoryPort subscriptionRepository;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        String currency = request.getCurrency() != null ? request.getCurrency() : "PEN";

        Subscription sub = Subscription.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .amount(Money.of(request.getAmount(), currency))
                .dueDay(request.getDueDay())
                .category(request.getCategory())
                .isPaidThisMonth(false)
                .provider(request.getProvider())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Subscription saved = subscriptionRepository.save(sub);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId) {
        return subscriptionRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse togglePaidStatus(UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        sub.togglePaidStatus();

        Subscription updated = subscriptionRepository.save(sub);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSubscription(UUID subscriptionId) {
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription not found with id: " + subscriptionId);
        }
        subscriptionRepository.delete(subscriptionId);
    }

    private SubscriptionResponse mapToResponse(Subscription sub) {
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .workspaceId(sub.getWorkspaceId())
                .name(sub.getName())
                .amount(sub.getAmount() != null ? sub.getAmount().amount() : null)
                .dueDay(sub.getDueDay())
                .category(sub.getCategory())
                .currency(sub.getAmount() != null ? sub.getAmount().currency() : "PEN")
                .isPaidThisMonth(sub.getIsPaidThisMonth())
                .provider(sub.getProvider())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .build();
    }
}

package com.dualis.api.service.impl;

import com.dualis.api.domain.model.Subscription;
import com.dualis.api.domain.repository.SubscriptionRepository;
import com.dualis.api.dto.request.CreateSubscriptionRequest;
import com.dualis.api.dto.response.SubscriptionResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        Subscription sub = Subscription.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .amount(request.getAmount())
                .dueDay(request.getDueDay())
                .category(request.getCategory())
                .currency(request.getCurrency() != null ? request.getCurrency() : "PEN")
                .provider(request.getProvider())
                .isPaidThisMonth(false)
                .build();

        Subscription saved = subscriptionRepository.save(sub);
        return SubscriptionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId) {
        return subscriptionRepository.findByWorkspaceId(workspaceId).stream()
                .map(SubscriptionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse togglePaidStatus(UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        sub.setIsPaidThisMonth(!Boolean.TRUE.equals(sub.getIsPaidThisMonth()));
        Subscription updated = subscriptionRepository.save(sub);
        return SubscriptionResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteSubscription(UUID subscriptionId) {
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription not found with id: " + subscriptionId);
        }
        subscriptionRepository.deleteById(subscriptionId);
    }
}

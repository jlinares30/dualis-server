package com.dualis.api.modules.subscription.application.service;

import com.dualis.api.dto.request.CreateSubscriptionRequest;
import com.dualis.api.dto.response.SubscriptionResponse;
import com.dualis.api.modules.subscription.domain.model.Subscription;
import com.dualis.api.modules.subscription.domain.repository.SubscriptionRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionApplicationServiceTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @InjectMocks
    private SubscriptionApplicationService subscriptionApplicationService;

    private UUID workspaceId;
    private UUID subscriptionId;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        subscription = Subscription.builder()
                .id(subscriptionId)
                .workspaceId(workspaceId)
                .name("Netflix 4K")
                .amount(Money.of(new BigDecimal("49.90"), "PEN"))
                .dueDay(15)
                .category("Streaming")
                .isPaidThisMonth(false)
                .provider("Netflix Inc")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create subscription successfully")
    void createSubscription_Success() {
        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .workspaceId(workspaceId)
                .name("Netflix 4K")
                .amount(new BigDecimal("49.90"))
                .dueDay(15)
                .category("Streaming")
                .currency("PEN")
                .provider("Netflix Inc")
                .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        SubscriptionResponse response = subscriptionApplicationService.createSubscription(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Netflix 4K");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("49.90"));
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should get subscriptions by workspace")
    void getSubscriptionsByWorkspace_Success() {
        when(subscriptionRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(subscription));

        List<SubscriptionResponse> result = subscriptionApplicationService.getSubscriptionsByWorkspace(workspaceId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Netflix 4K");
    }

    @Test
    @DisplayName("Should toggle paid status")
    void togglePaidStatus_Success() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        SubscriptionResponse response = subscriptionApplicationService.togglePaidStatus(subscriptionId);

        assertThat(response).isNotNull();
        assertThat(response.getIsPaidThisMonth()).isTrue();
    }

    @Test
    @DisplayName("Should delete subscription successfully")
    void deleteSubscription_Success() {
        when(subscriptionRepository.existsById(subscriptionId)).thenReturn(true);

        subscriptionApplicationService.deleteSubscription(subscriptionId);

        verify(subscriptionRepository, times(1)).delete(subscriptionId);
    }
}

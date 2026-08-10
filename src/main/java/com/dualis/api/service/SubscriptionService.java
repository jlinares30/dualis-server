package com.dualis.api.service;

import com.dualis.api.dto.request.CreateSubscriptionRequest;
import com.dualis.api.dto.response.SubscriptionResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);
    List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId);
    SubscriptionResponse togglePaidStatus(UUID subscriptionId);
    void deleteSubscription(UUID subscriptionId);
}

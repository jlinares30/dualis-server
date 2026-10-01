package com.dualis.api.modules.subscription.application.usecase;

import com.dualis.api.modules.subscription.dto.request.CreateSubscriptionRequest;
import com.dualis.api.modules.subscription.dto.request.SaveSalaryDistributionConfigRequest;
import com.dualis.api.modules.subscription.dto.response.SalaryDistributionConfigResponse;
import com.dualis.api.modules.subscription.dto.response.SubscriptionResponse;

import java.util.List;
import java.util.UUID;

public interface ManageSubscriptionUseCase {
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);
    List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId);
    SubscriptionResponse togglePaidStatus(UUID subscriptionId);
    void deleteSubscription(UUID subscriptionId);

    SalaryDistributionConfigResponse getSalaryDistributionConfig(UUID workspaceId, String userEmail);
    SalaryDistributionConfigResponse saveSalaryDistributionConfig(SaveSalaryDistributionConfigRequest request);
}

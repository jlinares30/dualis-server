package com.dualis.api.modules.subscription.application.usecase;

import com.dualis.api.modules.subscription.dto.request.CreateSubscriptionRequest;
import com.dualis.api.modules.subscription.dto.request.PaySubscriptionRequest;
import com.dualis.api.modules.subscription.dto.response.SubscriptionPaymentResponse;
import com.dualis.api.modules.subscription.dto.request.SaveSalaryDistributionConfigRequest;
import com.dualis.api.modules.subscription.dto.response.SalaryDistributionConfigResponse;
import com.dualis.api.modules.subscription.dto.response.SubscriptionResponse;

import java.util.List;
import java.util.UUID;

public interface ManageSubscriptionUseCase {
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);
    List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId);
    /** Subscriptions with their paid status for the given period (defaults to the current month). */
    List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId, Integer periodYear, Integer periodMonth);
    /** Legacy toggle: pays or un-pays the current month. */
    SubscriptionResponse togglePaidStatus(UUID subscriptionId);
    SubscriptionResponse paySubscription(UUID subscriptionId, PaySubscriptionRequest request);
    SubscriptionResponse undoPayment(UUID subscriptionId, Integer periodYear, Integer periodMonth);
    List<SubscriptionPaymentResponse> getPaymentHistory(UUID subscriptionId);
    void deleteSubscription(UUID subscriptionId);

    SalaryDistributionConfigResponse getSalaryDistributionConfig(UUID workspaceId, String userEmail);
    SalaryDistributionConfigResponse saveSalaryDistributionConfig(SaveSalaryDistributionConfigRequest request);
}

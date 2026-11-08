package com.dualis.api.modules.subscription.application.service;

import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.subscription.application.usecase.ManageSubscriptionUseCase;
import com.dualis.api.modules.subscription.domain.model.SalaryDistributionBranch;
import com.dualis.api.modules.subscription.domain.model.SalaryDistributionConfig;
import com.dualis.api.modules.subscription.domain.model.Subscription;
import com.dualis.api.modules.subscription.domain.repository.SalaryDistributionConfigRepositoryPort;
import com.dualis.api.modules.subscription.domain.repository.SubscriptionRepositoryPort;
import com.dualis.api.modules.subscription.dto.request.CreateSubscriptionRequest;
import com.dualis.api.modules.subscription.dto.request.SalaryDistributionBranchDto;
import com.dualis.api.modules.subscription.dto.request.SaveSalaryDistributionConfigRequest;
import com.dualis.api.modules.subscription.dto.response.SalaryDistributionConfigResponse;
import com.dualis.api.modules.subscription.dto.response.SubscriptionResponse;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionApplicationService implements ManageSubscriptionUseCase {

    private final VerifyWorkspaceAccessUseCase workspaceAccess;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final SalaryDistributionConfigRepositoryPort salaryDistributionConfigRepository;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        workspaceAccess.assertCurrentUserIsMember(request.getWorkspaceId());
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
        return SubscriptionResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        return subscriptionRepository.findByWorkspaceId(workspaceId).stream()
                .map(SubscriptionResponse::fromDomain)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse togglePaidStatus(UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));
        workspaceAccess.assertCurrentUserIsMember(sub.getWorkspaceId());

        sub.togglePaidStatus();

        Subscription updated = subscriptionRepository.save(sub);
        return SubscriptionResponse.fromDomain(updated);
    }

    @Override
    @Transactional
    public void deleteSubscription(UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));
        workspaceAccess.assertCurrentUserIsMember(sub.getWorkspaceId());
        subscriptionRepository.delete(subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryDistributionConfigResponse getSalaryDistributionConfig(UUID workspaceId, String requestedUserEmail) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        // A user can only read their own salary configuration; the requested email is ignored.
        String userEmail = workspaceAccess.currentUserEmail();

        Optional<SalaryDistributionConfig> configOpt =
                salaryDistributionConfigRepository.findByWorkspaceIdAndUserEmail(workspaceId, userEmail);

        if (configOpt.isEmpty()) {
            // Legacy configs saved without an owner are still returned, but never another member's config.
            configOpt = salaryDistributionConfigRepository.findByWorkspaceId(workspaceId)
                    .filter(c -> c.getUserEmail() == null || c.getUserEmail().isBlank()
                            || c.getUserEmail().equalsIgnoreCase(userEmail));
        }

        return configOpt.map(this::toResponse).orElseGet(() -> SalaryDistributionConfigResponse.builder()
                .workspaceId(workspaceId)
                .userEmail(userEmail)
                .enabled(true)
                .frequency("MONTHLY")
                .paymentDay(30)
                .distributionType("SPLIT")
                .autoExecute(false)
                .branches(new ArrayList<>())
                .build());
    }

    @Override
    @Transactional
    public SalaryDistributionConfigResponse saveSalaryDistributionConfig(SaveSalaryDistributionConfigRequest request) {
        workspaceAccess.assertCurrentUserIsMember(request.getWorkspaceId());
        // The configuration always belongs to the authenticated user.
        request.setUserEmail(workspaceAccess.currentUserEmail());
        List<SalaryDistributionBranch> domainBranches = new ArrayList<>();
        if (request.getBranches() != null) {
            domainBranches = request.getBranches().stream()
                    .map(b -> SalaryDistributionBranch.builder()
                            .id(b.getId() != null ? b.getId() : UUID.randomUUID().toString())
                            .destinationType(b.getDestinationType())
                            .targetAccountId(b.getTargetAccountId())
                            .targetAccountName(b.getTargetAccountName())
                            .targetInvestmentId(b.getTargetInvestmentId())
                            .targetInvestmentName(b.getTargetInvestmentName())
                            .targetWorkspaceId(b.getTargetWorkspaceId())
                            .workspaceType(b.getWorkspaceType())
                            .mode(b.getMode() != null ? b.getMode() : "PERCENTAGE")
                            .value(b.getValue())
                            .label(b.getLabel())
                            .build())
                    .toList();
        }

        Optional<SalaryDistributionConfig> existingOpt =
                salaryDistributionConfigRepository.findByWorkspaceIdAndUserEmail(request.getWorkspaceId(), request.getUserEmail());

        SalaryDistributionConfig toSave;
        if (existingOpt.isPresent()) {
            SalaryDistributionConfig existing = existingOpt.get();
            existing.update(
                    request.getEnabled(),
                    request.getFrequency(),
                    request.getPaymentDay(),
                    request.getDistributionType(),
                    request.getPrimaryAccountId(),
                    request.getAutoExecute(),
                    request.getLastExecutedDate(),
                    domainBranches
            );
            toSave = existing;
        } else {
            toSave = SalaryDistributionConfig.builder()
                    .id(UUID.randomUUID())
                    .workspaceId(request.getWorkspaceId())
                    .userEmail(request.getUserEmail())
                    .enabled(Boolean.TRUE.equals(request.getEnabled()))
                    .frequency(request.getFrequency() != null ? request.getFrequency() : "MONTHLY")
                    .paymentDay(request.getPaymentDay() != null ? request.getPaymentDay() : 30)
                    .distributionType(request.getDistributionType() != null ? request.getDistributionType() : "SPLIT")
                    .primaryAccountId(request.getPrimaryAccountId())
                    .autoExecute(Boolean.TRUE.equals(request.getAutoExecute()))
                    .lastExecutedDate(request.getLastExecutedDate())
                    .branches(domainBranches)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
        }

        SalaryDistributionConfig saved = salaryDistributionConfigRepository.save(toSave);
        return toResponse(saved);
    }

    private SalaryDistributionConfigResponse toResponse(SalaryDistributionConfig domain) {
        List<SalaryDistributionBranchDto> branchDtos = new ArrayList<>();
        if (domain.getBranches() != null) {
            branchDtos = domain.getBranches().stream()
                    .map(b -> SalaryDistributionBranchDto.builder()
                            .id(b.getId())
                            .destinationType(b.getDestinationType())
                            .targetAccountId(b.getTargetAccountId())
                            .targetAccountName(b.getTargetAccountName())
                            .targetInvestmentId(b.getTargetInvestmentId())
                            .targetInvestmentName(b.getTargetInvestmentName())
                            .targetWorkspaceId(b.getTargetWorkspaceId())
                            .workspaceType(b.getWorkspaceType())
                            .mode(b.getMode())
                            .value(b.getValue())
                            .label(b.getLabel())
                            .build())
                    .toList();
        }

        return SalaryDistributionConfigResponse.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .userEmail(domain.getUserEmail())
                .enabled(domain.getEnabled())
                .frequency(domain.getFrequency())
                .paymentDay(domain.getPaymentDay())
                .distributionType(domain.getDistributionType())
                .primaryAccountId(domain.getPrimaryAccountId())
                .autoExecute(domain.getAutoExecute())
                .lastExecutedDate(domain.getLastExecutedDate())
                .branches(branchDtos)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
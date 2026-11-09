package com.dualis.api.modules.subscription.application.service;

import com.dualis.api.modules.subscription.domain.model.SubscriptionPayment;
import com.dualis.api.modules.subscription.domain.repository.SubscriptionPaymentRepositoryPort;
import com.dualis.api.modules.subscription.dto.request.PaySubscriptionRequest;
import com.dualis.api.modules.subscription.dto.response.SubscriptionPaymentResponse;
import com.dualis.api.modules.transaction.application.usecase.ManageTransactionUseCase;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.dto.request.CreateTransactionRequest;
import com.dualis.api.modules.transaction.dto.response.TransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final SubscriptionPaymentRepositoryPort paymentRepository;
    private final ManageTransactionUseCase transactionUseCase;

    /** Time zone used to decide which month "now" belongs to. */
    @Value("${app.timezone:America/Lima}")
    private String timezone = "America/Lima";

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
        YearMonth current = currentPeriod();
        return SubscriptionResponse.fromDomain(saved, current.getYear(), current.getMonthValue(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId) {
        return getSubscriptionsByWorkspace(workspaceId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByWorkspace(UUID workspaceId, Integer periodYear, Integer periodMonth) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        YearMonth period = resolvePeriod(periodYear, periodMonth);

        Map<UUID, SubscriptionPayment> paymentsBySubscription = paymentRepository
                .findByWorkspaceIdAndPeriod(workspaceId, period.getYear(), period.getMonthValue()).stream()
                .collect(Collectors.toMap(SubscriptionPayment::getSubscriptionId, p -> p, (a, b) -> a));

        return subscriptionRepository.findByWorkspaceId(workspaceId).stream()
                .map(sub -> SubscriptionResponse.fromDomain(sub, period.getYear(), period.getMonthValue(),
                        paymentsBySubscription.get(sub.getId())))
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse togglePaidStatus(UUID subscriptionId) {
        Subscription sub = loadAuthorized(subscriptionId);
        YearMonth current = currentPeriod();
        boolean paid = paymentRepository
                .findBySubscriptionIdAndPeriod(sub.getId(), current.getYear(), current.getMonthValue())
                .isPresent();
        return paid
                ? undoPayment(subscriptionId, current.getYear(), current.getMonthValue())
                : paySubscription(subscriptionId, PaySubscriptionRequest.builder()
                        .periodYear(current.getYear())
                        .periodMonth(current.getMonthValue())
                        .build());
    }

    @Override
    @Transactional
    public SubscriptionResponse paySubscription(UUID subscriptionId, PaySubscriptionRequest request) {
        Subscription sub = loadAuthorized(subscriptionId);
        PaySubscriptionRequest req = request != null ? request : new PaySubscriptionRequest();
        YearMonth period = resolvePeriod(req.getPeriodYear(), req.getPeriodMonth());

        if (paymentRepository.findBySubscriptionIdAndPeriod(sub.getId(), period.getYear(), period.getMonthValue()).isPresent()) {
            throw new IllegalStateException(String.format("%s is already paid for %02d/%d",
                    sub.getName(), period.getMonthValue(), period.getYear()));
        }

        BigDecimal amount = req.getAmount() != null ? req.getAmount() : sub.getAmount().amount();
        String currency = sub.getAmount() != null ? sub.getAmount().currency() : "PEN";
        OffsetDateTime now = OffsetDateTime.now();

        UUID transactionId = null;
        if (req.getAccountId() != null) {
            // Record the real expense in the same database transaction as the payment.
            TransactionResponse tx = transactionUseCase.createTransaction(CreateTransactionRequest.builder()
                    .workspaceId(sub.getWorkspaceId())
                    .accountId(req.getAccountId())
                    .type(TransactionType.EXPENSE)
                    .amount(amount)
                    .currency(currency)
                    .description(String.format("Pago de servicio: %s (%02d/%d)",
                            sub.getName(), period.getMonthValue(), period.getYear()))
                    .transactionDate(now)
                    .build());
            transactionId = tx.getId();
        }

        SubscriptionPayment payment = paymentRepository.save(SubscriptionPayment.builder()
                .subscriptionId(sub.getId())
                .workspaceId(sub.getWorkspaceId())
                .periodYear(period.getYear())
                .periodMonth(period.getMonthValue())
                .amount(Money.of(amount, currency))
                .paidAt(now)
                .paidByEmail(workspaceAccess.currentUserEmail())
                .transactionId(transactionId)
                .createdAt(now)
                .build());

        return SubscriptionResponse.fromDomain(sub, period.getYear(), period.getMonthValue(), payment);
    }

    @Override
    @Transactional
    public SubscriptionResponse undoPayment(UUID subscriptionId, Integer periodYear, Integer periodMonth) {
        Subscription sub = loadAuthorized(subscriptionId);
        YearMonth period = resolvePeriod(periodYear, periodMonth);

        SubscriptionPayment payment = paymentRepository
                .findBySubscriptionIdAndPeriod(sub.getId(), period.getYear(), period.getMonthValue())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                        "No payment registered for %s in %02d/%d", sub.getName(), period.getMonthValue(), period.getYear())));

        if (payment.getTransactionId() != null) {
            try {
                // Reverts the account balance as well.
                transactionUseCase.deleteTransaction(payment.getTransactionId());
            } catch (ResourceNotFoundException alreadyDeleted) {
                // The user already removed the expense manually; nothing to revert.
            }
        }
        paymentRepository.delete(payment.getId());

        return SubscriptionResponse.fromDomain(sub, period.getYear(), period.getMonthValue(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPaymentResponse> getPaymentHistory(UUID subscriptionId) {
        Subscription sub = loadAuthorized(subscriptionId);
        return paymentRepository.findBySubscriptionId(sub.getId()).stream()
                .map(SubscriptionPaymentResponse::fromDomain)
                .toList();
    }

    private Subscription loadAuthorized(UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));
        workspaceAccess.assertCurrentUserIsMember(sub.getWorkspaceId());
        return sub;
    }

    private YearMonth currentPeriod() {
        String zone = timezone != null && !timezone.isBlank() ? timezone : "America/Lima";
        return YearMonth.now(ZoneId.of(zone));
    }

    private YearMonth resolvePeriod(Integer year, Integer month) {
        if (year == null && month == null) {
            return currentPeriod();
        }
        if (year == null || month == null) {
            throw new IllegalArgumentException("Both periodYear and periodMonth must be provided");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("periodMonth must be between 1 and 12");
        }
        return YearMonth.of(year, month);
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
package com.dualis.api.modules.transaction.application.service;

import com.dualis.api.modules.category.application.usecase.ManageCategoryUseCase;
import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.settlement.application.usecase.ManageSplitRuleUseCase;
import com.dualis.api.modules.settlement.dto.response.SplitRuleResponse;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.exception.ForbiddenException;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.transaction.application.usecase.ManageTransactionUseCase;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.transaction.dto.request.CreateTransactionRequest;
import com.dualis.api.modules.transaction.dto.request.UpdateTransactionRequest;
import com.dualis.api.modules.transaction.dto.response.TransactionResponse;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionApplicationService implements ManageTransactionUseCase {

    private final VerifyWorkspaceAccessUseCase workspaceAccess;
    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final WorkspaceRepositoryPort workspaceRepository;
    private final ManageCategoryUseCase categoryUseCase;
    private final ManageSplitRuleUseCase splitRuleUseCase;

    private static final BigDecimal DEFAULT_SPLIT_PERCENTAGE = new BigDecimal("50.00");

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        workspaceAccess.assertCurrentUserIsMember(request.getWorkspaceId());
        Account primaryAccount = findActiveAccount(request.getAccountId(), request.getWorkspaceId(), true);

        Account targetAccount = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getTargetAccountId() == null) {
                throw new IllegalArgumentException("Target account ID is required for TRANSFER transactions");
            }
            if (request.getAccountId().equals(request.getTargetAccountId())) {
                throw new IllegalArgumentException("Source and target accounts must be different for transfers");
            }
            targetAccount = findActiveAccount(request.getTargetAccountId(), request.getWorkspaceId(), true);
        }

        applyBalanceImpact(primaryAccount, targetAccount, request.getType(), request.getAmount());

        // The transaction is always recorded in the currency of the account it moves money from,
        // which is the currency its balance impact is applied in.
        Money moneyAmount = Money.of(request.getAmount(), primaryAccount.getCurrency());
        CategoryNature nature = resolveCategoryNature(request.getCategoryNature(), request.getCategoryId());
        BigDecimal splitPercentage = resolveSplitPercentage(
                request.getWorkspaceId(), request.getType(), request.getSplitPartnerAPercentage(), request.getSplitRuleId());

        Transaction transaction = Transaction.builder()
                .workspaceId(request.getWorkspaceId())
                .accountId(primaryAccount.getId())
                .accountName(primaryAccount.getName())
                .targetAccountId(targetAccount != null ? targetAccount.getId() : null)
                .targetAccountName(targetAccount != null ? targetAccount.getName() : null)
                .categoryId(request.getCategoryId())
                .type(request.getType())
                .categoryNature(nature)
                .amount(moneyAmount)
                .description(request.getDescription())
                .exchangeRate(request.getExchangeRate())
                .originalAmount(request.getOriginalAmount())
                .originalCurrency(request.getOriginalCurrency())
                .splitPartnerAPercentage(splitPercentage)
                .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByWorkspace(UUID workspaceId) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        return transactionRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            UUID workspaceId,
            UUID accountId,
            TransactionType type,
            UUID categoryId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String search,
            Pageable pageable
    ) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        return transactionRepository.findTransactions(
                workspaceId, accountId, type, categoryId, startDate, endDate, search, pageable
        ).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        workspaceAccess.assertCurrentUserIsMember(transaction.getWorkspaceId());
        return mapToResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(UUID id, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        workspaceAccess.assertCurrentUserIsMember(transaction.getWorkspaceId());

        // Revert previous balance impact
        revertBalanceImpact(transaction);

        // Accounts already linked to this transaction may stay (e.g. a partner editing a shared expense);
        // switching to a different account requires the user to have access to that account's workspace.
        Account primaryAccount = findActiveAccount(request.getAccountId(), transaction.getWorkspaceId(),
                !isAccountOfTransaction(request.getAccountId(), transaction));
        Account targetAccount = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getTargetAccountId() == null) {
                throw new IllegalArgumentException("Target account ID is required for TRANSFER transactions");
            }
            if (request.getAccountId().equals(request.getTargetAccountId())) {
                throw new IllegalArgumentException("Source and target accounts must be different for transfers");
            }
            targetAccount = findActiveAccount(request.getTargetAccountId(), transaction.getWorkspaceId(),
                    !isAccountOfTransaction(request.getTargetAccountId(), transaction));
        }

        // Apply new balance impact
        applyBalanceImpact(primaryAccount, targetAccount, request.getType(), request.getAmount());

        Money moneyAmount = Money.of(request.getAmount(), primaryAccount.getCurrency());
        CategoryNature nature = resolveCategoryNature(request.getCategoryNature(), request.getCategoryId());
        BigDecimal splitPercentage = request.getSplitPartnerAPercentage() != null || request.getSplitRuleId() != null
                ? resolveSplitPercentage(transaction.getWorkspaceId(), request.getType(),
                        request.getSplitPartnerAPercentage(), request.getSplitRuleId())
                : null; // keep the percentage stored when the expense was created

        transaction.updateDetails(
                primaryAccount.getId(),
                primaryAccount.getName(),
                targetAccount != null ? targetAccount.getId() : null,
                targetAccount != null ? targetAccount.getName() : null,
                request.getCategoryId(),
                request.getType(),
                nature,
                moneyAmount,
                request.getDescription(),
                request.getTransactionDate(),
                splitPercentage
        );

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        workspaceAccess.assertCurrentUserIsMember(transaction.getWorkspaceId());

        revertBalanceImpact(transaction);
        transactionRepository.delete(id);
    }

    private void revertBalanceImpact(Transaction transaction) {
        Account primaryAccount = findAccountById(transaction.getAccountId());
        Account targetAccount = transaction.getTargetAccountId() != null ? findAccountById(transaction.getTargetAccountId()) : null;
        // Revert exactly what applyBalanceImpact applied: the numeric amount in the primary account currency.
        // This also repairs legacy rows stored with a currency different from their account's.
        Money amount = Money.of(transaction.getAmount().amount(), primaryAccount.getCurrency());

        switch (transaction.getType()) {
            case INCOME -> primaryAccount.debit(amount);
            case EXPENSE -> primaryAccount.credit(amount);
            case TRANSFER -> {
                primaryAccount.credit(amount);
                if (targetAccount != null) {
                    targetAccount.debit(convertCurrencyIfDifferent(amount, targetAccount.getBalance().currency()));
                    accountRepository.save(targetAccount);
                }
            }
        }
        accountRepository.save(primaryAccount);
    }

    private void applyBalanceImpact(Account primaryAccount, Account targetAccount, TransactionType type, BigDecimal amountVal) {
        Money amount = Money.of(amountVal, primaryAccount.getBalance() != null ? primaryAccount.getBalance().currency() : Money.DEFAULT_CURRENCY);
        switch (type) {
            case INCOME -> primaryAccount.credit(amount);
            case EXPENSE -> primaryAccount.debit(amount);
            case TRANSFER -> {
                primaryAccount.debit(amount);
                targetAccount.credit(convertCurrencyIfDifferent(amount, targetAccount.getBalance().currency()));
                accountRepository.save(targetAccount);
            }
        }
        accountRepository.save(primaryAccount);
    }

    private boolean isAccountOfTransaction(UUID accountId, Transaction transaction) {
        return accountId != null
                && (accountId.equals(transaction.getAccountId()) || accountId.equals(transaction.getTargetAccountId()));
    }

    private Account findActiveAccount(UUID accountId, UUID workspaceId, boolean requireCurrentUserAccess) {
        Account account = findAccountById(accountId);

        // A user may only move money from/to accounts of workspaces they belong to
        // (prevents using a partner's personal account from the shared workspace).
        if (requireCurrentUserAccess
                && !account.getWorkspaceId().equals(workspaceId)
                && !workspaceAccess.isMember(account.getWorkspaceId(), workspaceAccess.currentUserEmail())) {
            throw new ForbiddenException("You do not have access to account " + accountId);
        }

        if (!account.getWorkspaceId().equals(workspaceId)) {
            boolean isAllowed = false;
            java.util.Optional<Workspace> targetWsOpt = workspaceRepository.findById(workspaceId);
            if (targetWsOpt.isPresent() && targetWsOpt.get().getType() == WorkspaceType.COUPLE) {
                java.util.Optional<Workspace> accountWsOpt = workspaceRepository.findById(account.getWorkspaceId());
                if (accountWsOpt.isPresent()) {
                    Workspace accountWs = accountWsOpt.get();
                    isAllowed = targetWsOpt.get().getMembers().stream()
                            .anyMatch(member -> member.getUserEmail().equalsIgnoreCase(accountWs.getOwnerEmail())
                                    || (accountWs.getMembers() != null && accountWs.getMembers().stream()
                                    .anyMatch(m -> m.getUserEmail().equalsIgnoreCase(member.getUserEmail()))));
                }
            }

            if (!isAllowed) {
                throw new IllegalArgumentException("Account " + accountId + " does not belong to workspace " + workspaceId);
            }
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account " + accountId + " is not ACTIVE");
        }
        return account;
    }

    private Account findAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.fromEntity(t);
    }

    private CategoryNature resolveCategoryNature(CategoryNature requested, UUID categoryId) {
        if (requested != null) {
            return requested;
        }
        if (categoryId == null) {
            return null;
        }
        // Derive ESSENTIAL / NON_ESSENTIAL from the category so dashboards can classify the expense.
        return categoryUseCase.getCategoryById(categoryId).getCategoryNature();
    }

    /**
     * Resolves and freezes the share of partner A for a shared expense, so later changes to the
     * workspace's split rule never rewrite the balance of past expenses.
     */
    private BigDecimal resolveSplitPercentage(UUID workspaceId, TransactionType type,
                                              BigDecimal requested, UUID splitRuleId) {
        if (type != TransactionType.EXPENSE) {
            return null;
        }
        boolean isCouple = workspaceRepository.findById(workspaceId)
                .map(w -> w.getType() == WorkspaceType.COUPLE)
                .orElse(false);
        if (!isCouple) {
            return null;
        }
        if (requested != null) {
            return requested.setScale(2, RoundingMode.HALF_UP);
        }
        java.util.Optional<SplitRuleResponse> rule = splitRuleId != null
                ? java.util.Optional.of(splitRuleUseCase.getSplitRuleById(splitRuleId))
                : splitRuleUseCase.getSplitRulesByWorkspace(workspaceId).stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsDefault()))
                        .findFirst();
        return rule.map(SplitRuleResponse::getPartnerAPercentage)
                .filter(java.util.Objects::nonNull)
                .map(p -> p.setScale(2, RoundingMode.HALF_UP))
                .orElse(DEFAULT_SPLIT_PERCENTAGE);
    }

    private static final Map<String, Double> FX_RATES_TO_USD = Map.of(
            "USD", 1.0,
            "EUR", 1.08,
            "PEN", 0.27, // 1 PEN = ~0.27 USD (3.75 PEN per USD)
            "GBP", 1.28,
            "MXN", 0.058,
            "COP", 0.00025,
            "CLP", 0.0011,
            "ARS", 0.0011,
            "BRL", 0.18
    );

    private Money convertCurrencyIfDifferent(Money sourceMoney, String targetCurrency) {
        if (sourceMoney == null || sourceMoney.currency().equalsIgnoreCase(targetCurrency)) {
            return sourceMoney;
        }

        String fromCurr = sourceMoney.currency().toUpperCase();
        String toCurr = targetCurrency.toUpperCase();

        Double fromRate = FX_RATES_TO_USD.get(fromCurr);
        Double toRate = FX_RATES_TO_USD.get(toCurr);

        if (fromRate == null || toRate == null || toRate == 0) {
            // Fallback: if unknown exchange rate, return same numerical amount in target currency
            return new Money(sourceMoney.amount(), toCurr);
        }

        // Amount in USD = amount * fromRate
        // Amount in target = (amount * fromRate) / toRate
        BigDecimal fromRateBd = BigDecimal.valueOf(fromRate);
        BigDecimal toRateBd = BigDecimal.valueOf(toRate);
        BigDecimal inUsd = sourceMoney.amount().multiply(fromRateBd);
        BigDecimal convertedAmount = inUsd.divide(toRateBd, 2, RoundingMode.HALF_UP);

        return new Money(convertedAmount, toCurr);
    }

}

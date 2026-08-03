package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.*;
import com.dualis.api.domain.specification.TransactionSpecification;
import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.request.SyncPullRequest;
import com.dualis.api.dto.request.SyncPushRequest;
import com.dualis.api.dto.response.*;
import com.dualis.api.service.SyncService;
import com.dualis.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SplitRuleRepository splitRuleRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final SettlementRepository settlementRepository;
    private final TransactionService transactionService;

    @Override
    @Transactional(readOnly = true)
    public SyncResponse pullDelta(SyncPullRequest request) {
        UUID workspaceId = request.getWorkspaceId();
        OffsetDateTime lastSyncedAt = request.getLastSyncedAt();
        OffsetDateTime serverTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        // 1. Delta Accounts
        List<Account> accounts = accountRepository.findByWorkspaceId(workspaceId);
        if (lastSyncedAt != null) {
            accounts = accounts.stream()
                    .filter(a -> a.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<AccountResponse> accountResponses = accounts.stream().map(AccountResponse::fromEntity).toList();

        // 2. Delta Transactions
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                workspaceId, null, null, null, null, null, null
        );
        List<Transaction> transactions = transactionRepository.findAll(spec);
        if (lastSyncedAt != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<TransactionResponse> transactionResponses = transactions.stream().map(TransactionResponse::fromEntity).toList();

        // 3. Delta Split Rules
        List<SplitRule> splitRules = splitRuleRepository.findByWorkspaceId(workspaceId);
        if (lastSyncedAt != null) {
            splitRules = splitRules.stream()
                    .filter(sr -> sr.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<SplitRuleResponse> splitRuleResponses = splitRules.stream().map(SplitRuleResponse::fromEntity).toList();

        // 4. Delta Budgets
        List<Budget> budgets = budgetRepository.findAll().stream()
                .filter(b -> b.getWorkspaceId().equals(workspaceId))
                .toList();
        if (lastSyncedAt != null) {
            budgets = budgets.stream()
                    .filter(b -> b.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<BudgetResponse> budgetResponses = budgets.stream().map(BudgetResponse::fromEntity).toList();

        // 5. Delta Categories
        List<Category> categories = categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId);
        if (lastSyncedAt != null) {
            categories = categories.stream()
                    .filter(c -> c.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<CategoryResponse> categoryResponses = categories.stream().map(CategoryResponse::fromEntity).toList();

        // 6. Delta Settlements
        List<Settlement> settlements = settlementRepository.findByWorkspaceId(workspaceId);
        if (lastSyncedAt != null) {
            settlements = settlements.stream()
                    .filter(s -> s.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<SettlementResponse> settlementResponses = settlements.stream().map(SettlementResponse::fromEntity).toList();

        int totalCount = accountResponses.size() + transactionResponses.size() + splitRuleResponses.size()
                + budgetResponses.size() + categoryResponses.size() + settlementResponses.size();

        return SyncResponse.builder()
                .workspaceId(workspaceId)
                .serverTimestamp(serverTimestamp)
                .accounts(accountResponses)
                .transactions(transactionResponses)
                .splitRules(splitRuleResponses)
                .budgets(budgetResponses)
                .categories(categoryResponses)
                .settlements(settlementResponses)
                .syncedCount(totalCount)
                .build();
    }

    @Override
    @Transactional
    public SyncResponse pushOfflineData(SyncPushRequest request) {
        OffsetDateTime pushStart = OffsetDateTime.now(ZoneOffset.UTC);

        if (request.getOfflineTransactions() != null) {
            for (CreateTransactionRequest txReq : request.getOfflineTransactions()) {
                txReq.setWorkspaceId(request.getWorkspaceId());
                transactionService.createTransaction(txReq);
            }
        }

        SyncPullRequest pullReq = SyncPullRequest.builder()
                .workspaceId(request.getWorkspaceId())
                .lastSyncedAt(pushStart.minusSeconds(1))
                .build();

        return pullDelta(pullReq);
    }
}

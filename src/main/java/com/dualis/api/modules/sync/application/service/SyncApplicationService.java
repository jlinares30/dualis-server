package com.dualis.api.modules.sync.application.service;

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.account.dto.response.AccountResponse;
import com.dualis.api.modules.budget.domain.model.Budget;
import com.dualis.api.modules.budget.domain.repository.BudgetRepositoryPort;
import com.dualis.api.modules.budget.dto.response.BudgetResponse;
import com.dualis.api.modules.category.domain.model.Category;
import com.dualis.api.modules.category.domain.repository.CategoryRepositoryPort;
import com.dualis.api.modules.category.dto.response.CategoryResponse;
import com.dualis.api.modules.settlement.domain.model.Settlement;
import com.dualis.api.modules.settlement.domain.model.SplitRule;
import com.dualis.api.modules.settlement.domain.repository.SettlementRepositoryPort;
import com.dualis.api.modules.settlement.domain.repository.SplitRuleRepositoryPort;
import com.dualis.api.modules.settlement.dto.response.SettlementResponse;
import com.dualis.api.modules.settlement.dto.response.SplitRuleResponse;
import com.dualis.api.modules.sync.application.usecase.ManageSyncUseCase;
import com.dualis.api.modules.sync.dto.request.SyncPullRequest;
import com.dualis.api.modules.sync.dto.request.SyncPushRequest;
import com.dualis.api.modules.sync.dto.response.SyncResponse;
import com.dualis.api.modules.transaction.application.usecase.ManageTransactionUseCase;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.transaction.dto.request.CreateTransactionRequest;
import com.dualis.api.modules.transaction.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyncApplicationService implements ManageSyncUseCase {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final SplitRuleRepositoryPort splitRuleRepository;
    private final BudgetRepositoryPort budgetRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final SettlementRepositoryPort settlementRepository;
    private final ManageTransactionUseCase transactionUseCase;

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
                    .filter(a -> a.getUpdatedAt() != null && a.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<AccountResponse> accountResponses = accounts.stream().map(AccountResponse::fromDomain).toList();

        // 2. Delta Transactions
        List<Transaction> transactions = transactionRepository.findByWorkspaceId(workspaceId);
        if (lastSyncedAt != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<TransactionResponse> transactionResponses = transactions.stream().map(TransactionResponse::fromEntity).toList();

        // 3. Delta Split Rules
        List<SplitRule> splitRules = splitRuleRepository.findByWorkspaceId(workspaceId);
        if (lastSyncedAt != null) {
            splitRules = splitRules.stream()
                    .filter(sr -> sr.getUpdatedAt() != null && sr.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<SplitRuleResponse> splitRuleResponses = splitRules.stream().map(SplitRuleResponse::fromEntity).toList();

        // 4. Delta Budgets
        List<Budget> budgets = budgetRepository.findByWorkspaceId(workspaceId);
        if (lastSyncedAt != null) {
            budgets = budgets.stream()
                    .filter(b -> b.getUpdatedAt() != null && b.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<BudgetResponse> budgetResponses = budgets.stream().map(BudgetResponse::fromDomain).toList();

        // 5. Delta Categories
        List<Category> categories = categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId);
        if (lastSyncedAt != null) {
            categories = categories.stream()
                    .filter(c -> c.getUpdatedAt() != null && c.getUpdatedAt().isAfter(lastSyncedAt))
                    .toList();
        }
        List<CategoryResponse> categoryResponses = categories.stream().map(CategoryResponse::fromDomain).toList();

        // 6. Delta Settlements
        List<Settlement> settlements = settlementRepository.findByWorkspaceId(workspaceId);
        if (lastSyncedAt != null) {
            settlements = settlements.stream()
                    .filter(s -> s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(lastSyncedAt))
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
                transactionUseCase.createTransaction(txReq);
            }
        }

        SyncPullRequest pullReq = SyncPullRequest.builder()
                .workspaceId(request.getWorkspaceId())
                .lastSyncedAt(pushStart.minusSeconds(1))
                .build();

        return pullDelta(pullReq);
    }
}

package com.dualis.api.modules.analytics.application.service;

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.analytics.application.usecase.GetDashboardSummaryUseCase;
import com.dualis.api.modules.analytics.dto.response.CategoryExpenseBreakdownResponse;
import com.dualis.api.modules.analytics.dto.response.DashboardSummaryResponse;
import com.dualis.api.modules.budget.domain.model.Budget;
import com.dualis.api.modules.budget.domain.repository.BudgetRepositoryPort;
import com.dualis.api.modules.category.domain.model.Category;
import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.category.domain.repository.CategoryRepositoryPort;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardApplicationService implements GetDashboardSummaryUseCase {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final BudgetRepositoryPort budgetRepository;
    private final WorkspaceRepositoryPort workspaceRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(UUID workspaceId, Integer month, Integer year) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int periodMonth = (month != null) ? month : now.getMonthValue();
        int periodYear = (year != null) ? year : now.getYear();
        int lastDay = YearMonth.of(periodYear, periodMonth).lengthOfMonth();

        OffsetDateTime startDate = OffsetDateTime.of(periodYear, periodMonth, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endDate = OffsetDateTime.of(periodYear, periodMonth, lastDay, 23, 59, 59, 999999999, ZoneOffset.UTC);

        // 1. Total Liquidity / Balance across active accounts
        List<Account> activeAccounts = accountRepository.findByWorkspaceIdAndStatus(workspaceId, AccountStatus.ACTIVE);
        BigDecimal totalBalance = activeAccounts.stream()
                .map(a -> a.getBalance() != null ? a.getBalance().amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Workspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
        String currency = workspace != null && workspace.getCurrency() != null ? workspace.getCurrency() : (!activeAccounts.isEmpty() && activeAccounts.get(0).getBalance() != null ? activeAccounts.get(0).getBalance().currency() : "USD");

        // 2. Income & Expense Cash Flow
        List<Transaction> incomeTransactions = transactionRepository.findTransactions(
                workspaceId, null, TransactionType.INCOME, null, startDate, endDate, null
        );
        BigDecimal monthlyIncome = incomeTransactions.stream()
                .map(t -> t.getAmount() != null ? t.getAmount().amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transaction> expenseTransactions = transactionRepository.findTransactions(
                workspaceId, null, TransactionType.EXPENSE, null, startDate, endDate, null
        );
        BigDecimal monthlyExpenses = expenseTransactions.stream()
                .map(t -> t.getAmount() != null ? t.getAmount().amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Net Savings & Savings Rate
        BigDecimal netSavings = monthlyIncome.subtract(monthlyExpenses);
        BigDecimal savingsRatePercentage = BigDecimal.ZERO;
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRatePercentage = netSavings.multiply(new BigDecimal("100"))
                    .divide(monthlyIncome, 2, RoundingMode.HALF_UP);
        }

        // 4. Essential vs Non-Essential Breakdown
        BigDecimal essentialExpenses = expenseTransactions.stream()
                .filter(t -> t.getCategoryNature() == CategoryNature.ESSENTIAL)
                .map(t -> t.getAmount() != null ? t.getAmount().amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal nonEssentialExpenses = expenseTransactions.stream()
                .filter(t -> t.getCategoryNature() == CategoryNature.NON_ESSENTIAL)
                .map(t -> t.getAmount() != null ? t.getAmount().amount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal essentialPercentage = BigDecimal.ZERO;
        BigDecimal nonEssentialPercentage = BigDecimal.ZERO;
        if (monthlyExpenses.compareTo(BigDecimal.ZERO) > 0) {
            essentialPercentage = essentialExpenses.multiply(new BigDecimal("100"))
                    .divide(monthlyExpenses, 2, RoundingMode.HALF_UP);
            nonEssentialPercentage = nonEssentialExpenses.multiply(new BigDecimal("100"))
                    .divide(monthlyExpenses, 2, RoundingMode.HALF_UP);
        }

        // 5. Category Expense Breakdown
        Map<UUID, Category> categoryMap = categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId).stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (existing, replacement) -> existing));

        Map<UUID, BigDecimal> categoryExpensesMap = new HashMap<>();
        for (Transaction expense : expenseTransactions) {
            UUID catId = expense.getCategoryId();
            if (catId != null) {
                BigDecimal amt = expense.getAmount() != null ? expense.getAmount().amount() : BigDecimal.ZERO;
                categoryExpensesMap.merge(catId, amt, BigDecimal::add);
            }
        }

        List<CategoryExpenseBreakdownResponse> categoryBreakdown = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> entry : categoryExpensesMap.entrySet()) {
            UUID catId = entry.getKey();
            BigDecimal amount = entry.getValue();
            Category cat = categoryMap.get(catId);

            String catName = (cat != null) ? cat.getName() : "Uncategorized";
            String icon = (cat != null) ? cat.getIcon() : "tag";
            String color = (cat != null) ? cat.getColor() : "#9CA3AF";

            BigDecimal pct = BigDecimal.ZERO;
            if (monthlyExpenses.compareTo(BigDecimal.ZERO) > 0) {
                pct = amount.multiply(new BigDecimal("100")).divide(monthlyExpenses, 2, RoundingMode.HALF_UP);
            }

            categoryBreakdown.add(CategoryExpenseBreakdownResponse.builder()
                    .categoryId(catId)
                    .categoryName(catName)
                    .icon(icon)
                    .color(color)
                    .amount(amount)
                    .percentageOfTotal(pct)
                    .build());
        }

        categoryBreakdown.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        // 6. Active & Exceeded Budgets Summary
        List<Budget> periodBudgets = budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, periodMonth, periodYear);
        int activeBudgetsCount = periodBudgets.size();
        int exceededBudgetsCount = 0;

        for (Budget b : periodBudgets) {
            BigDecimal budgetLimit = b.getAmount() != null ? b.getAmount().amount() : BigDecimal.ZERO;
            BigDecimal catSpent = expenseTransactions.stream()
                    .filter(t -> b.getCategoryId() == null || Objects.equals(t.getCategoryId(), b.getCategoryId()))
                    .map(t -> t.getAmount() != null ? t.getAmount().amount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (catSpent.compareTo(budgetLimit) > 0) {
                exceededBudgetsCount++;
            }
        }

        return DashboardSummaryResponse.builder()
                .workspaceId(workspaceId)
                .currency(currency)
                .periodMonth(periodMonth)
                .periodYear(periodYear)
                .totalBalance(totalBalance)
                .monthlyIncome(monthlyIncome)
                .monthlyExpenses(monthlyExpenses)
                .netSavings(netSavings)
                .savingsRatePercentage(savingsRatePercentage)
                .essentialExpenses(essentialExpenses)
                .nonEssentialExpenses(nonEssentialExpenses)
                .essentialPercentage(essentialPercentage)
                .nonEssentialPercentage(nonEssentialPercentage)
                .categoryBreakdown(categoryBreakdown)
                .activeBudgetsCount(activeBudgetsCount)
                .exceededBudgetsCount(exceededBudgetsCount)
                .build();
    }
}

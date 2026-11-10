package com.dualis.api.modules.analytics.application.service;

import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
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
import com.dualis.api.modules.exchangerate.application.usecase.ConvertCurrencyUseCase;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;
import com.dualis.api.shared.domain.valueobject.Money;
import com.dualis.api.shared.time.MonthPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardApplicationService implements GetDashboardSummaryUseCase {

    private final VerifyWorkspaceAccessUseCase workspaceAccess;
    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final BudgetRepositoryPort budgetRepository;
    private final WorkspaceRepositoryPort workspaceRepository;
    private final ConvertCurrencyUseCase currencyConverter;

    @Value("${app.timezone:America/Lima}")
    private String timezone = MonthPeriod.DEFAULT_ZONE;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(UUID workspaceId, Integer month, Integer year) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        YearMonth current = MonthPeriod.current(timezone);
        int periodMonth = (month != null) ? month : current.getMonthValue();
        int periodYear = (year != null) ? year : current.getYear();
        YearMonth period = YearMonth.of(periodYear, periodMonth);
        OffsetDateTime startDate = MonthPeriod.start(period, timezone);
        OffsetDateTime endDate = MonthPeriod.end(period, timezone);

        Workspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
        List<Account> workspaceAccounts = accountRepository.findByWorkspaceId(workspaceId);
        List<Account> activeAccounts = workspaceAccounts.stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .toList();
        String currency = workspace != null && workspace.getCurrency() != null
                ? workspace.getCurrency()
                : (!activeAccounts.isEmpty() ? activeAccounts.get(0).getCurrency() : Money.DEFAULT_CURRENCY);

        // 1. Total Liquidity: active accounts flagged "included in total", converted to the workspace currency
        BigDecimal totalBalance = activeAccounts.stream()
                .filter(Account::isIncludedInTotal)
                .map(a -> a.getBalance() != null
                        ? currencyConverter.convert(a.getBalance().amount(), a.getCurrency(), currency)
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Income & Expense Cash Flow. A personal workspace also includes movements of its accounts
        //    recorded in the couple workspace (e.g. shared expenses paid with a personal account).
        boolean personal = workspace != null && workspace.getType() == WorkspaceType.INDIVIDUAL;
        List<UUID> ownAccountIds = personal ? workspaceAccounts.stream().map(Account::getId).toList() : List.of();
        List<Transaction> incomeTransactions = personal
                ? transactionRepository.findByWorkspaceOrAccounts(workspaceId, ownAccountIds, TransactionType.INCOME, startDate, endDate)
                : transactionRepository.findTransactions(workspaceId, null, TransactionType.INCOME, null, startDate, endDate, null);
        List<Transaction> expenseTransactions = personal
                ? transactionRepository.findByWorkspaceOrAccounts(workspaceId, ownAccountIds, TransactionType.EXPENSE, startDate, endDate)
                : transactionRepository.findTransactions(workspaceId, null, TransactionType.EXPENSE, null, startDate, endDate, null);

        java.util.function.Function<Transaction, BigDecimal> inWsCurrency = t -> t.getAmount() != null
                ? currencyConverter.convert(t.getAmount().amount(), t.getAmount().currency(), currency)
                : BigDecimal.ZERO;

        BigDecimal monthlyIncome = incomeTransactions.stream().map(inWsCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthlyExpenses = expenseTransactions.stream().map(inWsCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);

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
                .map(inWsCurrency)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal nonEssentialExpenses = expenseTransactions.stream()
                .filter(t -> t.getCategoryNature() == CategoryNature.NON_ESSENTIAL)
                .map(inWsCurrency)
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
                categoryExpensesMap.merge(catId, inWsCurrency.apply(expense), BigDecimal::add);
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
            BigDecimal budgetLimit = b.getAmount() != null
                    ? currencyConverter.convert(b.getAmount().amount(), b.getAmount().currency(), currency)
                    : BigDecimal.ZERO;
            BigDecimal catSpent = expenseTransactions.stream()
                    .filter(t -> Objects.equals(t.getWorkspaceId(), workspaceId))
                    .filter(t -> b.getCategoryId() == null || Objects.equals(t.getCategoryId(), b.getCategoryId()))
                    .map(inWsCurrency)
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

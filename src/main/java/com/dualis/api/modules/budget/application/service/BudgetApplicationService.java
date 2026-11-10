package com.dualis.api.modules.budget.application.service;

import org.springframework.beans.factory.annotation.Value;
import com.dualis.api.shared.time.MonthPeriod;
import com.dualis.api.modules.exchangerate.application.usecase.ConvertCurrencyUseCase;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.budget.application.usecase.ManageBudgetUseCase;
import com.dualis.api.modules.budget.domain.model.Budget;
import com.dualis.api.modules.budget.domain.model.BudgetStatus;
import com.dualis.api.modules.budget.domain.repository.BudgetRepositoryPort;
import com.dualis.api.modules.budget.dto.request.CreateBudgetRequest;
import com.dualis.api.modules.budget.dto.request.UpdateBudgetRequest;
import com.dualis.api.modules.budget.dto.response.BudgetProgressResponse;
import com.dualis.api.modules.budget.dto.response.BudgetResponse;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetApplicationService implements ManageBudgetUseCase {

    private final VerifyWorkspaceAccessUseCase workspaceAccess;
    private final BudgetRepositoryPort budgetRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final ConvertCurrencyUseCase currencyConverter;

    @Value("${app.timezone:America/Lima}")
    private String timezone = MonthPeriod.DEFAULT_ZONE;

    @Override
    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        workspaceAccess.assertCurrentUserIsMember(request.getWorkspaceId());
        List<Budget> existingList = budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(
                request.getWorkspaceId(), request.getPeriodMonth(), request.getPeriodYear()
        );

        boolean duplicate = existingList.stream().anyMatch(b ->
                (b.getCategoryId() == null && request.getCategoryId() == null) ||
                        (b.getCategoryId() != null && b.getCategoryId().equals(request.getCategoryId()))
        );

        if (duplicate) {
            throw new IllegalArgumentException("A budget for this category and period already exists in the workspace");
        }

        Budget budget = Budget.builder()
                .workspaceId(request.getWorkspaceId())
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .amount(Money.of(request.getAmount(), request.getCurrency() != null ? request.getCurrency().toUpperCase() : Money.DEFAULT_CURRENCY))
                .periodMonth(request.getPeriodMonth())
                .periodYear(request.getPeriodYear())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Budget saved = budgetRepository.save(budget);
        return BudgetResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetProgressResponse> getBudgetsByWorkspaceAndPeriod(UUID workspaceId, Integer month, Integer year) {
        workspaceAccess.assertCurrentUserIsMember(workspaceId);
        List<Budget> list;
        if (month != null && year != null) {
            list = budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, month, year);
        } else {
            list = budgetRepository.findByWorkspaceId(workspaceId);
        }
        // One expense query per month instead of one lookup + query per budget.
        Map<YearMonth, List<Transaction>> expensesByMonth = new HashMap<>();
        return list.stream()
                .map(b -> buildProgress(b, expensesByMonth.computeIfAbsent(
                        YearMonth.of(b.getPeriodYear(), b.getPeriodMonth()),
                        ym -> monthExpenses(workspaceId, ym, null))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        workspaceAccess.assertCurrentUserIsMember(budget.getWorkspaceId());
        return BudgetResponse.fromDomain(budget);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(UUID id, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        workspaceAccess.assertCurrentUserIsMember(budget.getWorkspaceId());

        budget.updateDetails(
                request.getName(),
                null,
                request.getAmount(),
                budget.getAmount() != null ? budget.getAmount().currency() : Money.DEFAULT_CURRENCY,
                request.getPeriodMonth(),
                request.getPeriodYear()
        );

        Budget updated = budgetRepository.save(budget);
        return BudgetResponse.fromDomain(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetProgressResponse getBudgetProgress(UUID budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + budgetId));
        workspaceAccess.assertCurrentUserIsMember(budget.getWorkspaceId());
        YearMonth ym = YearMonth.of(budget.getPeriodYear(), budget.getPeriodMonth());
        return buildProgress(budget, monthExpenses(budget.getWorkspaceId(), ym, budget.getCategoryId()));
    }

    private List<Transaction> monthExpenses(UUID workspaceId, YearMonth ym, UUID categoryId) {
        return transactionRepository.findTransactions(
                workspaceId, null, TransactionType.EXPENSE, categoryId,
                MonthPeriod.start(ym, timezone), MonthPeriod.end(ym, timezone), null);
    }

    /** Progress of a budget given the workspace expenses of its month (filtered here by category). */
    private BudgetProgressResponse buildProgress(Budget budget, List<Transaction> monthExpenses) {
        String budgetCurrency = budget.getAmount() != null ? budget.getAmount().currency() : Money.DEFAULT_CURRENCY;
        BigDecimal spentAmount = monthExpenses.stream()
                .filter(t -> budget.getCategoryId() == null || budget.getCategoryId().equals(t.getCategoryId()))
                .map(t -> t.getAmount() != null
                        ? currencyConverter.convert(t.getAmount().amount(), t.getAmount().currency(), budgetCurrency)
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limitAmount = budget.getAmount() != null ? budget.getAmount().amount() : BigDecimal.ZERO;
        BigDecimal remainingAmount = limitAmount.subtract(spentAmount);

        BigDecimal spentPercentage = BigDecimal.ZERO;
        if (limitAmount.compareTo(BigDecimal.ZERO) > 0) {
            spentPercentage = spentAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(limitAmount, 2, RoundingMode.HALF_UP);
        }

        BudgetStatus status;
        if (spentPercentage.compareTo(new BigDecimal("80")) < 0) {
            status = BudgetStatus.ON_TRACK;
        } else if (spentPercentage.compareTo(new BigDecimal("100")) <= 0) {
            status = BudgetStatus.WARNING;
        } else {
            status = BudgetStatus.EXCEEDED;
        }

        return BudgetProgressResponse.builder()
                .budgetId(budget.getId())
                .workspaceId(budget.getWorkspaceId())
                .categoryId(budget.getCategoryId())
                .name(budget.getName())
                .limitAmount(limitAmount)
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .spentPercentage(spentPercentage)
                .currency(budget.getAmount() != null ? budget.getAmount().currency() : Money.DEFAULT_CURRENCY)
                .periodMonth(budget.getPeriodMonth())
                .periodYear(budget.getPeriodYear())
                .status(status)
                .build();
    }

    @Override
    @Transactional
    public void deleteBudget(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        workspaceAccess.assertCurrentUserIsMember(budget.getWorkspaceId());
        budgetRepository.delete(id);
    }
}

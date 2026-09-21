package com.dualis.api.modules.budget.application.service;

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
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetApplicationService implements ManageBudgetUseCase {

    private final BudgetRepositoryPort budgetRepository;
    private final TransactionRepositoryPort transactionRepository;

    @Override
    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request) {
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
                .amount(Money.of(request.getAmount(), request.getCurrency()))
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
    public List<BudgetResponse> getBudgetsByWorkspaceAndPeriod(UUID workspaceId, Integer month, Integer year) {
        List<Budget> list;
        if (month != null && year != null) {
            list = budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, month, year);
        } else {
            list = budgetRepository.findByWorkspaceId(workspaceId);
        }
        return list.stream().map(BudgetResponse::fromDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        return BudgetResponse.fromDomain(budget);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(UUID id, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        budget.updateDetails(
                request.getName(),
                null,
                request.getAmount(),
                budget.getAmount() != null ? budget.getAmount().currency() : "USD",
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

        YearMonth ym = YearMonth.of(budget.getPeriodYear(), budget.getPeriodMonth());
        OffsetDateTime startDate = ym.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDate = ym.atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        List<Transaction> expenses = transactionRepository.findTransactions(
                budget.getWorkspaceId(),
                null,
                TransactionType.EXPENSE,
                budget.getCategoryId(),
                startDate,
                endDate,
                null
        );

        BigDecimal spentAmount = expenses.stream()
                .map(t -> t.getAmount() != null ? t.getAmount().amount() : BigDecimal.ZERO)
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
                .currency(budget.getAmount() != null ? budget.getAmount().currency() : "USD")
                .periodMonth(budget.getPeriodMonth())
                .periodYear(budget.getPeriodYear())
                .status(status)
                .build();
    }

    @Override
    @Transactional
    public void deleteBudget(UUID id) {
        budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        budgetRepository.delete(id);
    }
}

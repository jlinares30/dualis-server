package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.BudgetRepository;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.domain.specification.TransactionSpecification;
import com.dualis.api.dto.request.CreateBudgetRequest;
import com.dualis.api.dto.request.UpdateBudgetRequest;
import com.dualis.api.dto.response.BudgetProgressResponse;
import com.dualis.api.dto.response.BudgetResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        // Prevent duplicate budget for same workspace, category, month, and year
        budgetRepository.findByWorkspaceIdAndCategoryIdAndPeriodMonthAndPeriodYear(
                request.getWorkspaceId(), request.getCategoryId(), request.getPeriodMonth(), request.getPeriodYear()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("A budget for this category and period already exists in the workspace");
        });

        Budget budget = Budget.builder()
                .workspaceId(request.getWorkspaceId())
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .periodMonth(request.getPeriodMonth())
                .periodYear(request.getPeriodYear())
                .build();

        Budget savedBudget = budgetRepository.save(budget);
        return BudgetResponse.fromEntity(savedBudget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByWorkspace(UUID workspaceId, Integer periodMonth, Integer periodYear) {
        List<Budget> budgets;
        if (periodMonth != null && periodYear != null) {
            budgets = budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, periodMonth, periodYear);
        } else {
            // Default to current month/year if omitted
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            int month = periodMonth != null ? periodMonth : now.getMonthValue();
            int year = periodYear != null ? periodYear : now.getYear();
            budgets = budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, month, year);
        }
        return budgets.stream()
                .map(BudgetResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(UUID id) {
        Budget budget = findEntityById(id);
        return BudgetResponse.fromEntity(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetProgressResponse getBudgetProgress(UUID id) {
        Budget budget = findEntityById(id);

        int month = budget.getPeriodMonth();
        int year = budget.getPeriodYear();
        int lastDay = YearMonth.of(year, month).lengthOfMonth();

        OffsetDateTime startDate = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endDate = OffsetDateTime.of(year, month, lastDay, 23, 59, 59, 999999999, ZoneOffset.UTC);

        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                budget.getWorkspaceId(),
                null,
                TransactionType.EXPENSE,
                budget.getCategoryId(),
                startDate,
                endDate,
                null
        );

        List<Transaction> expenses = transactionRepository.findAll(spec);

        BigDecimal spentAmount = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limitAmount = budget.getAmount();
        BigDecimal remainingAmount = limitAmount.subtract(spentAmount);

        BigDecimal spentPercentage = BigDecimal.ZERO;
        if (limitAmount.compareTo(BigDecimal.ZERO) > 0) {
            spentPercentage = spentAmount.multiply(new BigDecimal("100"))
                    .divide(limitAmount, 2, RoundingMode.HALF_UP);
        }

        BudgetStatus status;
        if (spentPercentage.compareTo(new BigDecimal("100.00")) >= 0) {
            status = BudgetStatus.EXCEEDED;
        } else if (spentPercentage.compareTo(new BigDecimal("80.00")) >= 0) {
            status = BudgetStatus.WARNING;
        } else {
            status = BudgetStatus.ON_TRACK;
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
                .currency(budget.getCurrency())
                .periodMonth(month)
                .periodYear(year)
                .status(status)
                .build();
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(UUID id, UpdateBudgetRequest request) {
        Budget budget = findEntityById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            budget.setName(request.getName());
        }
        if (request.getAmount() != null) {
            budget.setAmount(request.getAmount());
        }
        if (request.getPeriodMonth() != null) {
            budget.setPeriodMonth(request.getPeriodMonth());
        }
        if (request.getPeriodYear() != null) {
            budget.setPeriodYear(request.getPeriodYear());
        }

        Budget updatedBudget = budgetRepository.save(budget);
        return BudgetResponse.fromEntity(updatedBudget);
    }

    @Override
    @Transactional
    public void deleteBudget(UUID id) {
        Budget budget = findEntityById(id);
        budgetRepository.delete(budget);
    }

    private Budget findEntityById(UUID id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
    }
}

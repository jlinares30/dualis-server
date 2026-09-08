package com.dualis.api.modules.budget.application.service;

import com.dualis.api.domain.model.BudgetStatus;
import com.dualis.api.domain.model.Transaction;
import com.dualis.api.domain.model.TransactionType;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.domain.specification.TransactionSpecification;
import com.dualis.api.dto.request.CreateBudgetRequest;
import com.dualis.api.dto.request.UpdateBudgetRequest;
import com.dualis.api.dto.response.BudgetProgressResponse;
import com.dualis.api.dto.response.BudgetResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.budget.application.usecase.ManageBudgetUseCase;
import com.dualis.api.modules.budget.domain.model.Budget;
import com.dualis.api.modules.budget.domain.repository.BudgetRepositoryPort;
import com.dualis.api.service.BudgetService;
import com.dualis.api.shared.domain.valueobject.Money;
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
public class BudgetApplicationService implements ManageBudgetUseCase, BudgetService {

    private final BudgetRepositoryPort budgetRepository;
    private final TransactionRepository transactionRepository;

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
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByWorkspace(UUID workspaceId, Integer periodMonth, Integer periodYear) {
        return getBudgetsByWorkspaceAndPeriod(workspaceId, periodMonth, periodYear);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByWorkspaceAndPeriod(UUID workspaceId, Integer periodMonth, Integer periodYear) {
        List<Budget> budgets;
        if (periodMonth != null && periodYear != null) {
            budgets = budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, periodMonth, periodYear);
        } else {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            int month = periodMonth != null ? periodMonth : now.getMonthValue();
            int year = periodYear != null ? periodYear : now.getYear();
            budgets = budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, month, year);
        }
        return budgets.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        return mapToResponse(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetProgressResponse getBudgetProgress(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

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

        BigDecimal limitAmount = budget.getAmount() != null ? budget.getAmount().amount() : BigDecimal.ZERO;
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
                .currency(budget.getAmount() != null ? budget.getAmount().currency() : "USD")
                .periodMonth(month)
                .periodYear(year)
                .status(status)
                .build();
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
                null,
                request.getPeriodMonth(),
                request.getPeriodYear()
        );

        Budget updated = budgetRepository.save(budget);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBudget(UUID id) {
        budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        budgetRepository.delete(id);
    }

    private BudgetResponse mapToResponse(Budget b) {
        return BudgetResponse.builder()
                .id(b.getId())
                .workspaceId(b.getWorkspaceId())
                .categoryId(b.getCategoryId())
                .name(b.getName())
                .amount(b.getAmount() != null ? b.getAmount().amount() : null)
                .currency(b.getAmount() != null ? b.getAmount().currency() : "USD")
                .periodMonth(b.getPeriodMonth())
                .periodYear(b.getPeriodYear())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}

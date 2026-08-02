package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.BudgetRepository;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.dto.request.CreateBudgetRequest;
import com.dualis.api.dto.response.BudgetProgressResponse;
import com.dualis.api.dto.response.BudgetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private UUID workspaceId;
    private UUID categoryId;
    private UUID budgetId;
    private Budget budget;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        budgetId = UUID.randomUUID();

        budget = Budget.builder()
                .id(budgetId)
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .name("Monthly Groceries")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create budget successfully")
    void createBudget_Success() {
        CreateBudgetRequest request = CreateBudgetRequest.builder()
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .name("Monthly Groceries")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .build();

        when(budgetRepository.findByWorkspaceIdAndCategoryIdAndPeriodMonthAndPeriodYear(
                workspaceId, categoryId, 8, 2026)).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        BudgetResponse response = budgetService.createBudget(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(budgetId);
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("500.00"));
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when duplicate budget exists")
    void createBudget_Duplicate_ThrowsException() {
        CreateBudgetRequest request = CreateBudgetRequest.builder()
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .periodMonth(8)
                .periodYear(2026)
                .build();

        when(budgetRepository.findByWorkspaceIdAndCategoryIdAndPeriodMonthAndPeriodYear(
                workspaceId, categoryId, 8, 2026)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.createBudget(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A budget for this category and period already exists");
    }

    @Test
    @DisplayName("Should calculate real-time budget progress ON_TRACK (<80%)")
    void getBudgetProgress_OnTrack() {
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        Transaction expense = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("250.00")) // 50% of 500
                .build();

        @SuppressWarnings("unchecked")
        Specification<Transaction> anySpec = any(Specification.class);
        when(transactionRepository.findAll(anySpec)).thenReturn(List.of(expense));

        BudgetProgressResponse progress = budgetService.getBudgetProgress(budgetId);

        assertThat(progress.getSpentAmount()).isEqualTo(new BigDecimal("250.00"));
        assertThat(progress.getRemainingAmount()).isEqualTo(new BigDecimal("250.00"));
        assertThat(progress.getSpentPercentage()).isEqualTo(new BigDecimal("50.00"));
        assertThat(progress.getStatus()).isEqualTo(BudgetStatus.ON_TRACK);
    }

    @Test
    @DisplayName("Should calculate real-time budget progress EXCEEDED (>100%)")
    void getBudgetProgress_Exceeded() {
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        Transaction expense = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("550.00")) // 110% of 500
                .build();

        @SuppressWarnings("unchecked")
        Specification<Transaction> anySpec = any(Specification.class);
        when(transactionRepository.findAll(anySpec)).thenReturn(List.of(expense));

        BudgetProgressResponse progress = budgetService.getBudgetProgress(budgetId);

        assertThat(progress.getSpentAmount()).isEqualTo(new BigDecimal("550.00"));
        assertThat(progress.getRemainingAmount()).isEqualTo(new BigDecimal("-50.00"));
        assertThat(progress.getSpentPercentage()).isEqualTo(new BigDecimal("110.00"));
        assertThat(progress.getStatus()).isEqualTo(BudgetStatus.EXCEEDED);
    }
}

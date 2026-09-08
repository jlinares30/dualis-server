package com.dualis.api.modules.budget.application.service;

import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.dto.request.CreateBudgetRequest;
import com.dualis.api.dto.response.BudgetResponse;
import com.dualis.api.modules.budget.domain.model.Budget;
import com.dualis.api.modules.budget.domain.repository.BudgetRepositoryPort;
import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetApplicationServiceTest {

    @Mock
    private BudgetRepositoryPort budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetApplicationService budgetApplicationService;

    private UUID workspaceId;
    private UUID categoryId;
    private Budget budget;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        budget = Budget.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .categoryId(categoryId)
                .name("Groceries Budget")
                .amount(Money.of(new BigDecimal("500.00"), "USD"))
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
                .name("Groceries Budget")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .periodMonth(8)
                .periodYear(2026)
                .build();

        when(budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, 8, 2026))
                .thenReturn(List.of());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        BudgetResponse response = budgetApplicationService.createBudget(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Groceries Budget");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should get budget by ID successfully")
    void getBudgetById_Success() {
        when(budgetRepository.findById(budget.getId())).thenReturn(Optional.of(budget));

        BudgetResponse response = budgetApplicationService.getBudgetById(budget.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(budget.getId());
    }
}

package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.*;
import com.dualis.api.dto.response.DashboardSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private UUID workspaceId;
    private Account account;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        account = Account.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .name("Checking")
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should aggregate dashboard summary metrics correctly")
    void getDashboardSummary_Success() {
        when(accountRepository.findByWorkspaceIdAndStatus(workspaceId, AccountStatus.ACTIVE)).thenReturn(List.of(account));

        Transaction income = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("3000.00"))
                .build();

        Transaction expense = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("1000.00"))
                .categoryNature(CategoryNature.ESSENTIAL)
                .build();

        @SuppressWarnings("unchecked")
        Specification<Transaction> anySpec = any(Specification.class);
        when(transactionRepository.findAll(anySpec))
                .thenReturn(List.of(income)) // income call
                .thenReturn(List.of(expense)); // expense call

        when(categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId)).thenReturn(List.of());
        when(budgetRepository.findByWorkspaceIdAndPeriodMonthAndPeriodYear(workspaceId, 8, 2026)).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.getDashboardSummary(workspaceId, 8, 2026);

        assertThat(response).isNotNull();
        assertThat(response.getTotalBalance()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(response.getMonthlyIncome()).isEqualTo(new BigDecimal("3000.00"));
        assertThat(response.getMonthlyExpenses()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(response.getNetSavings()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(response.getSavingsRatePercentage()).isEqualTo(new BigDecimal("66.67"));
        assertThat(response.getEssentialExpenses()).isEqualTo(new BigDecimal("1000.00"));
    }
}

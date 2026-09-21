package com.dualis.api.modules.analytics.application.service;

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.model.AccountType;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.analytics.dto.response.DashboardSummaryResponse;
import com.dualis.api.modules.budget.domain.repository.BudgetRepositoryPort;
import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.category.domain.repository.CategoryRepositoryPort;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardApplicationServiceTest {

    @Mock
    private AccountRepositoryPort accountRepository;

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private CategoryRepositoryPort categoryRepository;

    @Mock
    private BudgetRepositoryPort budgetRepository;

    @Mock
    private WorkspaceRepositoryPort workspaceRepository;

    @InjectMocks
    private DashboardApplicationService dashboardService;

    private UUID workspaceId;
    private Account account;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();

        account = Account.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .name("Checking")
                .type(AccountType.BANK)
                .balance(Money.of(new BigDecimal("5000.00"), "USD"))
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
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
                .amount(Money.of(new BigDecimal("3000.00"), "USD"))
                .createdAt(OffsetDateTime.now())
                .build();

        Transaction expense = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .type(TransactionType.EXPENSE)
                .amount(Money.of(new BigDecimal("1000.00"), "USD"))
                .categoryNature(CategoryNature.ESSENTIAL)
                .createdAt(OffsetDateTime.now())
                .build();

        when(transactionRepository.findTransactions(
                eq(workspaceId), isNull(), eq(TransactionType.INCOME), isNull(), any(), any(), isNull()
        )).thenReturn(List.of(income));

        when(transactionRepository.findTransactions(
                eq(workspaceId), isNull(), eq(TransactionType.EXPENSE), isNull(), any(), any(), isNull()
        )).thenReturn(List.of(expense));

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

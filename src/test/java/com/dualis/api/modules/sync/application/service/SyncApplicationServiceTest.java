package com.dualis.api.modules.sync.application.service;

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.model.AccountType;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.budget.domain.repository.BudgetRepositoryPort;
import com.dualis.api.modules.category.domain.repository.CategoryRepositoryPort;
import com.dualis.api.modules.settlement.domain.repository.SettlementRepositoryPort;
import com.dualis.api.modules.settlement.domain.repository.SplitRuleRepositoryPort;
import com.dualis.api.modules.sync.dto.request.SyncPullRequest;
import com.dualis.api.modules.sync.dto.request.SyncPushRequest;
import com.dualis.api.modules.sync.dto.response.SyncResponse;
import com.dualis.api.modules.transaction.application.usecase.ManageTransactionUseCase;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.transaction.dto.request.CreateTransactionRequest;
import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SyncApplicationServiceTest {

    @Mock
    private AccountRepositoryPort accountRepository;

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private SplitRuleRepositoryPort splitRuleRepository;

    @Mock
    private BudgetRepositoryPort budgetRepository;

    @Mock
    private CategoryRepositoryPort categoryRepository;

    @Mock
    private SettlementRepositoryPort settlementRepository;

    @Mock
    private ManageTransactionUseCase transactionUseCase;

    @InjectMocks
    private SyncApplicationService syncService;

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
                .balance(Money.of(new BigDecimal("1000.00"), "USD"))
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should pull delta data for workspace successfully")
    void pullDelta_Success() {
        SyncPullRequest request = SyncPullRequest.builder()
                .workspaceId(workspaceId)
                .lastSyncedAt(null)
                .build();

        when(accountRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of(account));
        when(transactionRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of());
        when(splitRuleRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of());
        when(budgetRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of());
        when(categoryRepository.findByWorkspaceIdOrSystemDefault(any(UUID.class))).thenReturn(List.of());
        when(settlementRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of());

        SyncResponse response = syncService.pullDelta(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccounts()).hasSize(1);
        assertThat(response.getServerTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should process offline transaction batch push successfully")
    void pushOfflineData_Success() {
        CreateTransactionRequest txReq = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(account.getId())
                .amount(new BigDecimal("50.00"))
                .build();

        SyncPushRequest pushRequest = SyncPushRequest.builder()
                .workspaceId(workspaceId)
                .offlineTransactions(List.of(txReq))
                .build();

        when(accountRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of(account));
        when(transactionRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of());
        when(splitRuleRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of());
        when(budgetRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of());
        when(categoryRepository.findByWorkspaceIdOrSystemDefault(any(UUID.class))).thenReturn(List.of());
        when(settlementRepository.findByWorkspaceId(any(UUID.class))).thenReturn(List.of());

        SyncResponse response = syncService.pushOfflineData(pushRequest);

        assertThat(response).isNotNull();
        verify(transactionUseCase, times(1)).createTransaction(any(CreateTransactionRequest.class));
    }
}

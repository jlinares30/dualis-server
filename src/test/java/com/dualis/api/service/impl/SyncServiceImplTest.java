package com.dualis.api.service.impl;

import com.dualis.api.domain.model.Account;
import com.dualis.api.domain.model.AccountType;
import com.dualis.api.domain.repository.*;
import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.request.SyncPullRequest;
import com.dualis.api.dto.request.SyncPushRequest;
import com.dualis.api.dto.response.SyncResponse;
import com.dualis.api.service.TransactionService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SplitRuleRepository splitRuleRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private SyncServiceImpl syncService;

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
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
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

        when(accountRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(account));
        when(transactionRepository.findAll(any())).thenReturn(List.of());
        when(splitRuleRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of());
        when(budgetRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId)).thenReturn(List.of());
        when(settlementRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of());

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

        when(accountRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(account));
        when(transactionRepository.findAll(any())).thenReturn(List.of());

        SyncResponse response = syncService.pushOfflineData(pushRequest);

        assertThat(response).isNotNull();
        verify(transactionService, times(1)).createTransaction(any(CreateTransactionRequest.class));
    }
}

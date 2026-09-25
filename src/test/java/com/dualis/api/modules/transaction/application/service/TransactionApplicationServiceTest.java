package com.dualis.api.modules.transaction.application.service;

import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.model.AccountType;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.workspace.domain.repository.WorkspaceRepositoryPort;
import com.dualis.api.modules.category.domain.model.CategoryNature;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.model.TransactionType;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.modules.transaction.dto.request.CreateTransactionRequest;
import com.dualis.api.modules.transaction.dto.response.TransactionResponse;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionApplicationServiceTest {

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private AccountRepositoryPort accountRepository;

    @Mock
    private WorkspaceRepositoryPort workspaceRepository;

    @InjectMocks
    private TransactionApplicationService transactionApplicationService;

    private UUID workspaceId;
    private UUID primaryAccountId;
    private UUID targetAccountId;
    private Account primaryAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        primaryAccountId = UUID.randomUUID();
        targetAccountId = UUID.randomUUID();

        primaryAccount = Account.builder()
                .id(primaryAccountId)
                .workspaceId(workspaceId)
                .name("Primary Account")
                .type(AccountType.BANK)
                .balance(Money.of(new BigDecimal("500.00"), "USD"))
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();

        targetAccount = Account.builder()
                .id(targetAccountId)
                .workspaceId(workspaceId)
                .name("Target Account")
                .type(AccountType.BANK)
                .balance(Money.of(new BigDecimal("200.00"), "USD"))
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create INCOME transaction through DDD application service")
    void createTransaction_Income_Success() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .description("Bonus")
                .build();

        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));

        Transaction savedTx = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .accountName("Primary Account")
                .type(TransactionType.INCOME)
                .amount(Money.of(new BigDecimal("150.00"), "USD"))
                .description("Bonus")
                .createdAt(OffsetDateTime.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionApplicationService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(primaryAccount.getBalance().amount()).isEqualTo(new BigDecimal("650.00"));
        verify(accountRepository, times(1)).save(primaryAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should create TRANSFER transaction and update both balances")
    void createTransaction_Transfer_Success() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .targetAccountId(targetAccountId)
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .description("Transfer")
                .build();

        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));
        when(accountRepository.findById(targetAccountId)).thenReturn(Optional.of(targetAccount));

        Transaction savedTx = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .accountName("Primary Account")
                .targetAccountId(targetAccountId)
                .targetAccountName("Target Account")
                .type(TransactionType.TRANSFER)
                .amount(Money.of(new BigDecimal("100.00"), "USD"))
                .description("Transfer")
                .createdAt(OffsetDateTime.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionApplicationService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(primaryAccount.getBalance().amount()).isEqualTo(new BigDecimal("400.00"));
        assertThat(targetAccount.getBalance().amount()).isEqualTo(new BigDecimal("300.00"));
        verify(accountRepository, times(1)).save(primaryAccount);
        verify(accountRepository, times(1)).save(targetAccount);
    }
}

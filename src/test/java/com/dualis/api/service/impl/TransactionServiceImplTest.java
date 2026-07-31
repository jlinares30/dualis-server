package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.AccountRepository;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.request.UpdateTransactionRequest;
import com.dualis.api.dto.response.TransactionResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

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
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        targetAccount = Account.builder()
                .id(targetAccountId)
                .workspaceId(workspaceId)
                .name("Target Account")
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should process INCOME transaction and increase account balance")
    void createTransaction_Income_Success() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .description("Salary bonus")
                .build();

        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));

        Transaction savedTx = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .account(primaryAccount)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .description("Salary bonus")
                .createdAt(OffsetDateTime.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(primaryAccount.getBalance()).isEqualTo(new BigDecimal("650.00"));
        verify(accountRepository, times(1)).save(primaryAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should process EXPENSE transaction and decrease account balance")
    void createTransaction_Expense_Success() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .description("Groceries")
                .build();

        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));

        Transaction savedTx = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .account(primaryAccount)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .description("Groceries")
                .createdAt(OffsetDateTime.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(primaryAccount.getBalance()).isEqualTo(new BigDecimal("450.00"));
        verify(accountRepository, times(1)).save(primaryAccount);
    }

    @Test
    @DisplayName("Should process TRANSFER transaction and adjust both accounts balances")
    void createTransaction_Transfer_Success() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .targetAccountId(targetAccountId)
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .description("Internal transfer")
                .build();

        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));
        when(accountRepository.findById(targetAccountId)).thenReturn(Optional.of(targetAccount));

        Transaction savedTx = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .account(primaryAccount)
                .targetAccount(targetAccount)
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .description("Internal transfer")
                .createdAt(OffsetDateTime.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(primaryAccount.getBalance()).isEqualTo(new BigDecimal("400.00"));
        assertThat(targetAccount.getBalance()).isEqualTo(new BigDecimal("300.00"));
        verify(accountRepository, times(1)).save(primaryAccount);
        verify(accountRepository, times(1)).save(targetAccount);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when transfer source and target accounts are identical")
    void createTransaction_Transfer_SameAccount_ThrowsException() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .targetAccountId(primaryAccountId)
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source and target accounts must be different");
    }

    @Test
    @DisplayName("Should get paginated transactions using specification")
    void getTransactions_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .account(primaryAccount)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .createdAt(OffsetDateTime.now())
                .build();

        Page<Transaction> page = new PageImpl<>(List.of(tx), pageable, 1);

        @SuppressWarnings("unchecked")
        Specification<Transaction> anySpec = any(Specification.class);
        when(transactionRepository.findAll(anySpec, eq(pageable))).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getTransactions(
                workspaceId, null, null, null, null, null, null, pageable
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should revert balance and delete transaction successfully")
    void deleteTransaction_Success() {
        UUID txId = UUID.randomUUID();
        Transaction tx = Transaction.builder()
                .id(txId)
                .workspaceId(workspaceId)
                .account(primaryAccount)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

        transactionService.deleteTransaction(txId);

        // Balance should revert from 500 to 400 because deleting INCOME removes the added funds
        assertThat(primaryAccount.getBalance()).isEqualTo(new BigDecimal("400.00"));
        verify(transactionRepository, times(1)).delete(tx);
    }
}

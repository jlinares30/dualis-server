package com.dualis.api.modules.transaction.application.service;

import com.dualis.api.domain.model.Account;
import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.domain.model.TransactionType;
import com.dualis.api.domain.repository.AccountRepository;
import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.request.UpdateTransactionRequest;
import com.dualis.api.dto.response.TransactionResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.transaction.application.usecase.ManageTransactionUseCase;
import com.dualis.api.modules.transaction.domain.model.Transaction;
import com.dualis.api.modules.transaction.domain.repository.TransactionRepositoryPort;
import com.dualis.api.service.TransactionService;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionApplicationService implements ManageTransactionUseCase, TransactionService {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        Account primaryAccount = findActiveAccount(request.getAccountId(), request.getWorkspaceId());

        Account targetAccount = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getTargetAccountId() == null) {
                throw new IllegalArgumentException("Target account ID is required for TRANSFER transactions");
            }
            if (request.getAccountId().equals(request.getTargetAccountId())) {
                throw new IllegalArgumentException("Source and target accounts must be different for transfers");
            }
            targetAccount = findActiveAccount(request.getTargetAccountId(), request.getWorkspaceId());
        }

        applyBalanceImpact(primaryAccount, targetAccount, request.getType(), request.getAmount());

        Money moneyAmount = Money.of(request.getAmount(), request.getCurrency() != null ? request.getCurrency() : Money.DEFAULT_CURRENCY);

        Transaction transaction = Transaction.builder()
                .workspaceId(request.getWorkspaceId())
                .accountId(primaryAccount.getId())
                .accountName(primaryAccount.getName())
                .targetAccountId(targetAccount != null ? targetAccount.getId() : null)
                .targetAccountName(targetAccount != null ? targetAccount.getName() : null)
                .categoryId(request.getCategoryId())
                .type(request.getType())
                .categoryNature(request.getCategoryNature())
                .amount(moneyAmount)
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByWorkspace(UUID workspaceId) {
        return transactionRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            UUID workspaceId,
            UUID accountId,
            TransactionType type,
            UUID categoryId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String search,
            Pageable pageable
    ) {
        return transactionRepository.findTransactions(
                workspaceId, accountId, type, categoryId, startDate, endDate, search, pageable
        ).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return mapToResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(UUID id, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Revert previous balance impact
        revertBalanceImpact(transaction);

        Account primaryAccount = findActiveAccount(request.getAccountId(), transaction.getWorkspaceId());
        Account targetAccount = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getTargetAccountId() == null) {
                throw new IllegalArgumentException("Target account ID is required for TRANSFER transactions");
            }
            if (request.getAccountId().equals(request.getTargetAccountId())) {
                throw new IllegalArgumentException("Source and target accounts must be different for transfers");
            }
            targetAccount = findActiveAccount(request.getTargetAccountId(), transaction.getWorkspaceId());
        }

        // Apply new balance impact
        applyBalanceImpact(primaryAccount, targetAccount, request.getType(), request.getAmount());

        Money moneyAmount = Money.of(request.getAmount(), request.getCurrency() != null ? request.getCurrency() : Money.DEFAULT_CURRENCY);

        transaction.updateDetails(
                primaryAccount.getId(),
                primaryAccount.getName(),
                targetAccount != null ? targetAccount.getId() : null,
                targetAccount != null ? targetAccount.getName() : null,
                request.getCategoryId(),
                request.getType(),
                request.getCategoryNature(),
                moneyAmount,
                request.getDescription(),
                request.getTransactionDate()
        );

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        revertBalanceImpact(transaction);
        transactionRepository.delete(id);
    }

    private void revertBalanceImpact(Transaction transaction) {
        Account primaryAccount = findAccountById(transaction.getAccountId());
        Account targetAccount = transaction.getTargetAccountId() != null ? findAccountById(transaction.getTargetAccountId()) : null;
        BigDecimal amount = transaction.getAmount().amount();

        switch (transaction.getType()) {
            case INCOME -> primaryAccount.setBalance(primaryAccount.getBalance().subtract(amount));
            case EXPENSE -> primaryAccount.setBalance(primaryAccount.getBalance().add(amount));
            case TRANSFER -> {
                primaryAccount.setBalance(primaryAccount.getBalance().add(amount));
                if (targetAccount != null) {
                    targetAccount.setBalance(targetAccount.getBalance().subtract(amount));
                    accountRepository.save(targetAccount);
                }
            }
        }
        accountRepository.save(primaryAccount);
    }

    private void applyBalanceImpact(Account primaryAccount, Account targetAccount, TransactionType type, BigDecimal amount) {
        switch (type) {
            case INCOME -> primaryAccount.setBalance(primaryAccount.getBalance().add(amount));
            case EXPENSE -> primaryAccount.setBalance(primaryAccount.getBalance().subtract(amount));
            case TRANSFER -> {
                primaryAccount.setBalance(primaryAccount.getBalance().subtract(amount));
                targetAccount.setBalance(targetAccount.getBalance().add(amount));
                accountRepository.save(targetAccount);
            }
        }
        accountRepository.save(primaryAccount);
    }

    private Account findActiveAccount(UUID accountId, UUID workspaceId) {
        Account account = findAccountById(accountId);

        if (!account.getWorkspaceId().equals(workspaceId)) {
            throw new IllegalArgumentException("Account " + accountId + " does not belong to workspace " + workspaceId);
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account " + accountId + " is not ACTIVE");
        }
        return account;
    }

    private Account findAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .workspaceId(t.getWorkspaceId())
                .accountId(t.getAccountId())
                .accountName(t.getAccountName())
                .targetAccountId(t.getTargetAccountId())
                .targetAccountName(t.getTargetAccountName())
                .categoryId(t.getCategoryId())
                .type(t.getType())
                .categoryNature(t.getCategoryNature())
                .amount(t.getAmount() != null ? t.getAmount().amount() : null)
                .currency(t.getAmount() != null ? t.getAmount().currency() : Money.DEFAULT_CURRENCY)
                .description(t.getDescription())
                .transactionDate(t.getTransactionDate())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}

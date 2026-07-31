package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.AccountRepository;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.domain.specification.TransactionSpecification;
import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.request.UpdateTransactionRequest;
import com.dualis.api.dto.response.TransactionResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
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

        Transaction transaction = Transaction.builder()
                .workspaceId(request.getWorkspaceId())
                .account(primaryAccount)
                .targetAccount(targetAccount)
                .categoryId(request.getCategoryId())
                .type(request.getType())
                .categoryNature(request.getCategoryNature())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByWorkspace(UUID workspaceId) {
        return transactionRepository.findByWorkspaceIdOrderByTransactionDateDesc(workspaceId).stream()
                .map(TransactionResponse::fromEntity)
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
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                workspaceId, accountId, type, categoryId, startDate, endDate, search
        );
        return transactionRepository.findAll(spec, pageable).map(TransactionResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return TransactionResponse.fromEntity(transaction);
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

        transaction.setAccount(primaryAccount);
        transaction.setTargetAccount(targetAccount);
        transaction.setCategoryId(request.getCategoryId());
        transaction.setType(request.getType());
        transaction.setCategoryNature(request.getCategoryNature());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDescription(request.getDescription());
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(updatedTransaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        revertBalanceImpact(transaction);
        transactionRepository.delete(transaction);
    }

    private void revertBalanceImpact(Transaction transaction) {
        Account primaryAccount = transaction.getAccount();
        Account targetAccount = transaction.getTargetAccount();
        BigDecimal amount = transaction.getAmount();

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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        if (!account.getWorkspaceId().equals(workspaceId)) {
            throw new IllegalArgumentException("Account " + accountId + " does not belong to workspace " + workspaceId);
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account " + accountId + " is not ACTIVE");
        }
        return account;
    }
}

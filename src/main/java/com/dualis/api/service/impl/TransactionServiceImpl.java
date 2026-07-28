package com.dualis.api.service.impl;

import com.dualis.api.domain.model.*;
import com.dualis.api.domain.repository.AccountRepository;
import com.dualis.api.domain.repository.TransactionRepository;
import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.response.TransactionResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Update balances atomically
        switch (request.getType()) {
            case INCOME -> primaryAccount.setBalance(primaryAccount.getBalance().add(request.getAmount()));
            case EXPENSE -> primaryAccount.setBalance(primaryAccount.getBalance().subtract(request.getAmount()));
            case TRANSFER -> {
                primaryAccount.setBalance(primaryAccount.getBalance().subtract(request.getAmount()));
                targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));
                accountRepository.save(targetAccount);
            }
        }
        accountRepository.save(primaryAccount);

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
    public TransactionResponse getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return TransactionResponse.fromEntity(transaction);
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

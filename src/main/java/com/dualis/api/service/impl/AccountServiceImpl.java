package com.dualis.api.service.impl;

import com.dualis.api.domain.model.Account;
import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.domain.repository.AccountRepository;
import com.dualis.api.dto.request.CreateAccountRequest;
import com.dualis.api.dto.request.UpdateAccountRequest;
import com.dualis.api.dto.response.AccountResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Deprecated in favor of com.dualis.api.modules.account.application.service.AccountApplicationService
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = Account.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .type(request.getType())
                .balance(request.getBalance())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .isIncludedInTotal(request.getIsIncludedInTotal() != null ? request.getIsIncludedInTotal() : true)
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(savedAccount);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(UUID id, UpdateAccountRequest request) {
        Account account = findEntityById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            account.setName(request.getName());
        }
        if (request.getType() != null) {
            account.setType(request.getType());
        }
        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            account.setDescription(request.getDescription());
        }
        if (request.getIsIncludedInTotal() != null) {
            account.setIsIncludedInTotal(request.getIsIncludedInTotal());
        }

        Account updatedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(updatedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByWorkspace(UUID workspaceId, AccountStatus status) {
        List<Account> accounts;
        if (status != null) {
            accounts = accountRepository.findByWorkspaceIdAndStatus(workspaceId, status);
        } else {
            accounts = accountRepository.findByWorkspaceId(workspaceId);
        }
        return accounts.stream()
                .map(AccountResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID id) {
        return AccountResponse.fromEntity(findEntityById(id));
    }

    @Override
    @Transactional
    public void archiveAccount(UUID id) {
        Account account = findEntityById(id);
        account.setStatus(AccountStatus.ARCHIVED);
        accountRepository.save(account);
    }

    private Account findEntityById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }
}

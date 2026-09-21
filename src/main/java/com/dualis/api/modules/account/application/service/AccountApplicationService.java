package com.dualis.api.modules.account.application.service;

import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.account.application.usecase.ManageAccountUseCase;
import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.model.AccountType;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.modules.account.dto.request.CreateAccountRequest;
import com.dualis.api.modules.account.dto.request.UpdateAccountRequest;
import com.dualis.api.modules.account.dto.response.AccountResponse;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountApplicationService implements ManageAccountUseCase {

    private final AccountRepositoryPort accountRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Money balance = Money.of(
                request.getBalance(),
                request.getCurrency() != null ? request.getCurrency() : Money.DEFAULT_CURRENCY
        );

        Account account = Account.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName())
                .type(request.getType())
                .balance(balance)
                .status(AccountStatus.ACTIVE)
                .description(request.getDescription())
                .isIncludedInTotal(request.getIsIncludedInTotal() != null ? request.getIsIncludedInTotal() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Account saved = accountRepository.save(account);
        return AccountResponse.fromDomain(saved);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(UUID id, UpdateAccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        account.updateDetails(
                request.getName(),
                request.getType(),
                request.getStatus(),
                request.getDescription(),
                request.getIsIncludedInTotal()
        );

        Account updated = accountRepository.save(account);
        return AccountResponse.fromDomain(updated);
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
        return accounts.stream().map(AccountResponse::fromDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return AccountResponse.fromDomain(account);
    }

    @Override
    @Transactional
    public void archiveAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        account.archive();
        accountRepository.save(account);
    }
}

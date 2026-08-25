package com.dualis.api.modules.account.application.service;

import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.domain.model.AccountType;
import com.dualis.api.dto.request.CreateAccountRequest;
import com.dualis.api.dto.request.UpdateAccountRequest;
import com.dualis.api.dto.response.AccountResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.account.application.usecase.ManageAccountUseCase;
import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
import com.dualis.api.service.AccountService;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountApplicationService implements ManageAccountUseCase, AccountService {

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
                .type(request.getType() != null ? com.dualis.api.modules.account.domain.model.AccountType.valueOf(request.getType().name()) : null)
                .balance(balance)
                .status(com.dualis.api.modules.account.domain.model.AccountStatus.ACTIVE)
                .description(request.getDescription())
                .isIncludedInTotal(request.getIsIncludedInTotal() != null ? request.getIsIncludedInTotal() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(UUID id, UpdateAccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        com.dualis.api.modules.account.domain.model.AccountType domainType = request.getType() != null
                ? com.dualis.api.modules.account.domain.model.AccountType.valueOf(request.getType().name())
                : null;
        com.dualis.api.modules.account.domain.model.AccountStatus domainStatus = request.getStatus() != null
                ? com.dualis.api.modules.account.domain.model.AccountStatus.valueOf(request.getStatus().name())
                : null;

        account.updateDetails(
                request.getName(),
                domainType,
                domainStatus,
                request.getDescription(),
                request.getIsIncludedInTotal()
        );

        Account updated = accountRepository.save(account);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByWorkspace(UUID workspaceId, AccountStatus status) {
        List<Account> accounts;
        if (status != null) {
            com.dualis.api.modules.account.domain.model.AccountStatus domainStatus =
                    com.dualis.api.modules.account.domain.model.AccountStatus.valueOf(status.name());
            accounts = accountRepository.findByWorkspaceIdAndStatus(workspaceId, domainStatus);
        } else {
            accounts = accountRepository.findByWorkspaceId(workspaceId);
        }
        return accounts.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public void archiveAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        account.archive();
        accountRepository.save(account);
    }

    private AccountResponse mapToResponse(Account a) {
        return AccountResponse.builder()
                .id(a.getId())
                .workspaceId(a.getWorkspaceId())
                .name(a.getName())
                .type(a.getType() != null ? AccountType.valueOf(a.getType().name()) : null)
                .balance(a.getBalance() != null ? a.getBalance().amount() : null)
                .currency(a.getBalance() != null ? a.getBalance().currency() : Money.DEFAULT_CURRENCY)
                .status(a.getStatus() != null ? AccountStatus.valueOf(a.getStatus().name()) : null)
                .description(a.getDescription())
                .isIncludedInTotal(a.isIncludedInTotal())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}

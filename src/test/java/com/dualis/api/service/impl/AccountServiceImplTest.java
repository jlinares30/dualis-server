package com.dualis.api.service.impl;

import com.dualis.api.domain.model.Account;
import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.domain.model.AccountType;
import com.dualis.api.domain.repository.AccountRepository;
import com.dualis.api.dto.request.CreateAccountRequest;
import com.dualis.api.dto.request.UpdateAccountRequest;
import com.dualis.api.dto.response.AccountResponse;
import com.dualis.api.exception.ResourceNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private UUID workspaceId;
    private UUID accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        account = Account.builder()
                .id(accountId)
                .workspaceId(workspaceId)
                .name("Savings Account")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .description("Personal savings")
                .isIncludedInTotal(true)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create account successfully")
    void createAccount_Success() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .workspaceId(workspaceId)
                .name("Savings Account")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .description("Personal savings")
                .isIncludedInTotal(true)
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.createAccount(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(accountId);
        assertThat(response.getName()).isEqualTo("Savings Account");
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should get account by ID successfully")
    void getAccountById_Success() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccountById(accountId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(accountId);
        assertThat(response.getName()).isEqualTo("Savings Account");
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when account ID does not exist")
    void getAccountById_NotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(accountId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found with id");
    }

    @Test
    @DisplayName("Should get accounts by workspace without status filter")
    void getAccountsByWorkspace_All() {
        when(accountRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(account));

        List<AccountResponse> responses = accountService.getAccountsByWorkspace(workspaceId, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(accountId);
        verify(accountRepository, times(1)).findByWorkspaceId(workspaceId);
    }

    @Test
    @DisplayName("Should get accounts by workspace with status filter")
    void getAccountsByWorkspace_FilteredByStatus() {
        when(accountRepository.findByWorkspaceIdAndStatus(workspaceId, AccountStatus.ACTIVE)).thenReturn(List.of(account));

        List<AccountResponse> responses = accountService.getAccountsByWorkspace(workspaceId, AccountStatus.ACTIVE);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(accountRepository, times(1)).findByWorkspaceIdAndStatus(workspaceId, AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should update account fields successfully")
    void updateAccount_Success() {
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .name("Updated Savings")
                .type(AccountType.INVESTMENT)
                .status(AccountStatus.ACTIVE)
                .description("Updated notes")
                .isIncludedInTotal(false)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.updateAccount(accountId, request);

        assertThat(response.getName()).isEqualTo("Updated Savings");
        assertThat(response.getType()).isEqualTo(AccountType.INVESTMENT);
        assertThat(response.getIsIncludedInTotal()).isFalse();
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("Should archive account setting status to ARCHIVED")
    void archiveAccount_Success() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.archiveAccount(accountId);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ARCHIVED);
        verify(accountRepository, times(1)).save(account);
    }
}

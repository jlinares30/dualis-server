package com.dualis.api.modules.account.application.service;

import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.domain.model.AccountType;
import com.dualis.api.dto.request.CreateAccountRequest;
import com.dualis.api.dto.request.UpdateAccountRequest;
import com.dualis.api.dto.response.AccountResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.account.domain.model.Account;
import com.dualis.api.modules.account.domain.repository.AccountRepositoryPort;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountApplicationServiceTest {

    @Mock
    private AccountRepositoryPort accountRepository;

    @InjectMocks
    private AccountApplicationService accountApplicationService;

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
                .name("Main Checking")
                .type(com.dualis.api.modules.account.domain.model.AccountType.BANK)
                .balance(Money.of(new BigDecimal("1000.00"), "USD"))
                .status(com.dualis.api.modules.account.domain.model.AccountStatus.ACTIVE)
                .isIncludedInTotal(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create account via DDD application service")
    void createAccount_Success() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .workspaceId(workspaceId)
                .name("Main Checking")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountApplicationService.createAccount(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(accountId);
        assertThat(response.getName()).isEqualTo("Main Checking");
        assertThat(response.getBalance()).isEqualByComparingTo("1000.00");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should update account details")
    void updateAccount_Success() {
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .name("Updated Checking")
                .type(AccountType.SAVINGS)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountApplicationService.updateAccount(accountId, request);

        assertThat(response).isNotNull();
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("Should archive account")
    void archiveAccount_Success() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountApplicationService.archiveAccount(accountId);

        assertThat(account.getStatus()).isEqualTo(com.dualis.api.modules.account.domain.model.AccountStatus.ARCHIVED);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("Should get accounts by workspace")
    void getAccountsByWorkspace_Success() {
        when(accountRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(account));

        List<AccountResponse> responses = accountApplicationService.getAccountsByWorkspace(workspaceId, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Main Checking");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when account not found")
    void getAccountById_NotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountApplicationService.getAccountById(accountId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

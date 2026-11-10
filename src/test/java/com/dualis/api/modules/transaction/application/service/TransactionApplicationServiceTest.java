package com.dualis.api.modules.transaction.application.service;

import org.mockito.Spy;
import com.dualis.api.modules.exchangerate.application.service.ExchangeRateApplicationService;
import com.dualis.api.modules.exchangerate.application.usecase.ConvertCurrencyUseCase;
import java.util.List;
import com.dualis.api.modules.workspace.domain.model.WorkspaceType;
import com.dualis.api.modules.workspace.domain.model.Workspace;
import com.dualis.api.modules.settlement.dto.response.SplitRuleResponse;
import com.dualis.api.modules.settlement.application.usecase.ManageSplitRuleUseCase;
import com.dualis.api.modules.category.dto.response.CategoryResponse;
import com.dualis.api.modules.category.application.usecase.ManageCategoryUseCase;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.dualis.api.exception.ForbiddenException;
import com.dualis.api.modules.workspace.application.usecase.VerifyWorkspaceAccessUseCase;
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

    @Mock
    private VerifyWorkspaceAccessUseCase workspaceAccess;

    @Mock
    private ManageCategoryUseCase categoryUseCase;

    @Mock
    private ManageSplitRuleUseCase splitRuleUseCase;

    /** Real converter with the provider disabled: uses the built-in reference rates (PEN 3.75 per USD). */
    @Spy
    private ConvertCurrencyUseCase currencyConverter = new ExchangeRateApplicationService(java.util.Optional::empty);

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

    @Test
    @DisplayName("Should reject using an account from a workspace the user does not belong to")
    void createTransaction_ForeignAccount_Forbidden() {
        UUID foreignWorkspaceId = UUID.randomUUID();
        Account foreignAccount = Account.builder()
                .id(UUID.randomUUID())
                .workspaceId(foreignWorkspaceId)
                .name("Partner Personal Account")
                .type(AccountType.BANK)
                .balance(Money.of(new BigDecimal("900.00"), "USD"))
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(foreignAccount.getId())
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        when(accountRepository.findById(foreignAccount.getId())).thenReturn(Optional.of(foreignAccount));
        when(workspaceAccess.currentUserEmail()).thenReturn("maria@example.com");
        when(workspaceAccess.isMember(foreignWorkspaceId, "maria@example.com")).thenReturn(false);

        assertThatThrownBy(() -> transactionApplicationService.createTransaction(request))
                .isInstanceOf(ForbiddenException.class);
        assertThat(foreignAccount.getBalance().amount()).isEqualByComparingTo("900.00");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not reveal a transaction from a workspace the user does not belong to")
    void getTransactionById_NotMember_Forbidden() {
        UUID txId = UUID.randomUUID();
        Transaction tx = Transaction.builder()
                .id(txId)
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .type(TransactionType.EXPENSE)
                .amount(Money.of(new BigDecimal("10.00"), "USD"))
                .createdAt(OffsetDateTime.now())
                .build();
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
        doThrow(new ForbiddenException("denied")).when(workspaceAccess).assertCurrentUserIsMember(workspaceId);

        assertThatThrownBy(() -> transactionApplicationService.getTransactionById(txId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Stores account currency, derives category nature and freezes the default split for couple expenses")
    void createTransaction_CoupleExpense_DerivesNatureAndSplit() {
        UUID categoryId = UUID.randomUUID();
        Workspace couple = Workspace.builder().id(workspaceId).name("Pareja").type(WorkspaceType.COUPLE)
                .currency("USD").ownerEmail("jorge@example.com").isActive(true).build();
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)
                .categoryId(categoryId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("80.00"))
                .currency("PEN") // wrong currency sent by the client: the account is in USD
                .build();

        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(couple));
        when(categoryUseCase.getCategoryById(categoryId)).thenReturn(
                CategoryResponse.builder().id(categoryId).categoryNature(CategoryNature.ESSENTIAL).build());
        when(splitRuleUseCase.getSplitRulesByWorkspace(workspaceId)).thenReturn(List.of(
                SplitRuleResponse.builder().isDefault(true).partnerAPercentage(new BigDecimal("60")).build()));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionApplicationService.createTransaction(request);

        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getCategoryNature()).isEqualTo(CategoryNature.ESSENTIAL);
        assertThat(response.getSplitPartnerAPercentage()).isEqualByComparingTo("60.00");
        assertThat(primaryAccount.getBalance().amount()).isEqualByComparingTo("420.00");
    }

    @Test
    @DisplayName("Deleting a legacy transaction stored in another currency reverts in the account currency instead of failing")
    void deleteTransaction_LegacyCurrencyMismatch_Reverts() {
        UUID txId = UUID.randomUUID();
        Transaction legacy = Transaction.builder()
                .id(txId).workspaceId(workspaceId).accountId(primaryAccountId)
                .type(TransactionType.EXPENSE)
                .amount(Money.of(new BigDecimal("30.00"), "PEN")) // account is USD
                .createdAt(OffsetDateTime.now())
                .build();
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(legacy));
        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));

        transactionApplicationService.deleteTransaction(txId);

        assertThat(primaryAccount.getBalance().amount()).isEqualByComparingTo("530.00");
        verify(transactionRepository).delete(txId);
    }

    @Test
    @DisplayName("Cross-currency transfer honors the user's rate and deleting it reverts with the same stored rate")
    void transfer_CrossCurrency_UsesAndRevertsStoredRate() {
        Account penAccount = Account.builder().id(UUID.randomUUID()).workspaceId(workspaceId).name("Soles")
                .type(AccountType.BANK).balance(Money.of(new BigDecimal("0.00"), "PEN"))
                .status(AccountStatus.ACTIVE).createdAt(OffsetDateTime.now()).build();
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(primaryAccountId)          // 500 USD
                .targetAccountId(penAccount.getId())
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("100.00"))
                .exchangeRate(new BigDecimal("3.70"))  // the rate the user actually got at the bank
                .build();
        when(accountRepository.findById(primaryAccountId)).thenReturn(Optional.of(primaryAccount));
        when(accountRepository.findById(penAccount.getId())).thenReturn(Optional.of(penAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse created = transactionApplicationService.createTransaction(request);

        assertThat(created.getExchangeRate()).isEqualByComparingTo("3.70");
        assertThat(primaryAccount.getBalance().amount()).isEqualByComparingTo("400.00");
        assertThat(penAccount.getBalance().amount()).isEqualByComparingTo("370.00");

        Transaction stored = Transaction.builder().id(UUID.randomUUID()).workspaceId(workspaceId)
                .accountId(primaryAccountId).targetAccountId(penAccount.getId()).type(TransactionType.TRANSFER)
                .amount(Money.of(new BigDecimal("100.00"), "USD")).exchangeRate(new BigDecimal("3.70"))
                .createdAt(OffsetDateTime.now()).build();
        when(transactionRepository.findById(stored.getId())).thenReturn(Optional.of(stored));

        transactionApplicationService.deleteTransaction(stored.getId());

        assertThat(primaryAccount.getBalance().amount()).isEqualByComparingTo("500.00");
        assertThat(penAccount.getBalance().amount()).isEqualByComparingTo("0.00");
    }
}

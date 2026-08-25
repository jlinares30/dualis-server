package com.dualis.api.modules.account.domain.model;

import com.dualis.api.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDomainTest {

    @Test
    @DisplayName("Should adjust balance on credit and debit operations")
    void testCreditAndDebit() {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .workspaceId(UUID.randomUUID())
                .name("Savings")
                .type(AccountType.SAVINGS)
                .balance(Money.of(new BigDecimal("1000.00"), "USD"))
                .status(AccountStatus.ACTIVE)
                .isIncludedInTotal(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        account.credit(Money.of(new BigDecimal("250.00"), "USD"));
        assertThat(account.getBalance().amount()).isEqualByComparingTo("1250.00");

        account.debit(Money.of(new BigDecimal("100.00"), "USD"));
        assertThat(account.getBalance().amount()).isEqualByComparingTo("1150.00");
    }

    @Test
    @DisplayName("Should update account details and status correctly")
    void testUpdateAndArchive() {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .workspaceId(UUID.randomUUID())
                .name("Old Name")
                .type(AccountType.BANK)
                .balance(Money.of(new BigDecimal("500.00"), "USD"))
                .status(AccountStatus.ACTIVE)
                .isIncludedInTotal(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        assertThat(account.isActive()).isTrue();

        account.updateDetails("New Checking", AccountType.BANK, AccountStatus.ACTIVE, "Salary account", false);
        assertThat(account.getName()).isEqualTo("New Checking");
        assertThat(account.isIncludedInTotal()).isFalse();

        account.archive();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ARCHIVED);
        assertThat(account.isActive()).isFalse();
    }
}

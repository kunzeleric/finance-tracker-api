package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

import static org.assertj.core.api.Assertions.*;

class AccountTest {
  Account account;

  @BeforeEach
  void setup() {
    account = Account.create("Nubank Conta Corrente", BigDecimal.valueOf(1000.00), AccountType.CHECKING);
  }

  @Nested
  class Constructor {

    @Test
    void shouldCreateAccountWithValidInitialBalance() {
      assertThat(account.getName()).isEqualTo("Nubank Conta Corrente");
      assertThat(account.getInitialBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
      assertThat(account.getCurrentBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
      assertThat(account.getType()).isEqualTo(AccountType.CHECKING);
      assertThat(account.getCreationDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldThrowExceptionWhenInitialBalanceIsNegative() {
      assertThatThrownBy(() -> Account.create("Poupança", BigDecimal.valueOf(-10.00), AccountType.SAVINGS))
          .isInstanceOf(InvalidBalanceException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
      assertThatThrownBy(() -> Account.create("", BigDecimal.valueOf(10.00), AccountType.SAVINGS))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenAccountTypeIsEmpty() {
      assertThatThrownBy(() -> Account.create("", BigDecimal.valueOf(10.00), null))
          .isInstanceOf(ValidationException.class);
    }
  }
}

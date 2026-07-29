package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;

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
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenAccountTypeIsEmpty() {
      assertThatThrownBy(() -> Account.create("", BigDecimal.valueOf(10.00), null))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class Deposit {

    @Test
    void shouldIncreaseBalanceWhenDepositingValidAmount() {
      BigDecimal initialBalance = account.getCurrentBalance();
      BigDecimal depositedAmount = BigDecimal.valueOf(10.00);
      BigDecimal finalAmount = initialBalance.add(depositedAmount);

      account.deposit(depositedAmount);
      assertThat(account.getCurrentBalance()).isEqualByComparingTo(finalAmount);
    }

    @Test
    void shouldThrowExceptionWhenDepositingNegativeAmount() {
      assertThatThrownBy(() -> account.deposit(BigDecimal.valueOf(-10.00)))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenDepositingZeroAmount() {
      assertThatThrownBy(() -> account.deposit(BigDecimal.valueOf(0.00))).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class Withdraw {

    @Test
    void shouldDecreaseBalanceWhenWithdrawingValidAmount() {
      BigDecimal initialBalance = account.getCurrentBalance();
      BigDecimal withdrawnAmount = BigDecimal.valueOf(10.00);
      BigDecimal finalAmount = initialBalance.subtract(withdrawnAmount);

      account.withdraw(withdrawnAmount);
      assertThat(account.getCurrentBalance()).isEqualByComparingTo(finalAmount);
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingNegativeAmount() {
      assertThatThrownBy(() -> account.withdraw(BigDecimal.valueOf(-10.00)))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingZeroAmount() {
      assertThatThrownBy(() -> account.withdraw(BigDecimal.valueOf(0.00))).isInstanceOf(IllegalArgumentException.class);
    }
  }
}

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
    account = Account.create("Nubank Conta Corrente", new BigDecimal(1000.00), AccountType.CHECKING);
  }

  @Nested
  class Constructor {

    @Test
    void shouldCreateAccountWithValidInitialBalance() {
      assertThat(account.getName()).isEqualTo("Nubank Conta Corrente");
      assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal(1000.00));
      assertThat(account.getType()).isEqualTo(AccountType.CHECKING);
      assertThat(account.getCreationDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldThrowExceptionWhenInitialBalanceIsNegative() {
      assertThatThrownBy(() -> Account.create("Poupança", new BigDecimal(-10.00), AccountType.SAVINGS))
          .isInstanceOf(InvalidBalanceException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
      assertThatThrownBy(() -> Account.create("", new BigDecimal(10.00), AccountType.SAVINGS))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenAccountTypeIsEmpty() {
      assertThatThrownBy(() -> Account.create("", new BigDecimal(10.00), null))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class Deposit {

    @Test
    void shouldIncreaseBalanceWhenDepositingValidAmount() {
      BigDecimal initialBalance = account.getBalance();
      BigDecimal depositedAmount = new BigDecimal(10.00);
      BigDecimal finalAmount = initialBalance.add(depositedAmount);

      account.deposit(depositedAmount);
      assertThat(account.getBalance()).isEqualByComparingTo(finalAmount);
    }

    @Test
    void shouldThrowExceptionWhenDepositingNegativeAmount() {
      assertThatThrownBy(() -> account.deposit(new BigDecimal(-10.00))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenDepositingZeroAmount() {
      assertThatThrownBy(() -> account.deposit(new BigDecimal(0.00))).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class Withdraw {

    @Test
    void shouldDecreaseBalanceWhenWithdrawingValidAmount() {
      BigDecimal initialBalance = account.getBalance();
      BigDecimal withdrawnAmount = new BigDecimal(10.00);
      BigDecimal finalAmount = initialBalance.subtract(withdrawnAmount);

      account.withdraw(withdrawnAmount);
      assertThat(account.getBalance()).isEqualByComparingTo(finalAmount);
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingNegativeAmount() {
      assertThatThrownBy(() -> account.withdraw(new BigDecimal(-10.00))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingZeroAmount() {
      assertThatThrownBy(() -> account.withdraw(new BigDecimal(0.00))).isInstanceOf(IllegalArgumentException.class);
    }
  }
}

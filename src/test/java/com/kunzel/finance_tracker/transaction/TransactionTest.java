package com.kunzel.finance_tracker.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.account.AccountTestFixtures;
import com.kunzel.finance_tracker.category.CategoryTestFixtures;

public class TransactionTest {
  Category category;
  Account account;

  @BeforeEach
  void setup() {
    category = CategoryTestFixtures.defaultCategory();
    account = AccountTestFixtures.defaultAccount();
  }

  @Nested
  class Constructor {
    @Test
    void shouldCreateTransaction() {
      Transaction transaction = Transaction.create("Transação teste", TransactionType.EXPENSE,
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          category);

      assertEquals(TransactionType.EXPENSE, transaction.getType());
      assertEquals(BigDecimal.valueOf(100.00), transaction.getAmount());
      assertEquals(account, transaction.getAccount());
      assertEquals(category, transaction.getCategory());
    }

    @Test
    void shouldThrowExceptionWhenTransactionValueIsZero() {
      assertThatThrownBy(
          () -> Transaction.create("Transação teste", TransactionType.EXPENSE, BigDecimal.valueOf(0.00),
              LocalDate.now(), account, category))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenTransactionDateIsEmpty() {
      assertThatThrownBy(
          () -> Transaction.create("Transação teste", TransactionType.EXPENSE, BigDecimal.valueOf(10.00), null,
              account, category))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenTransactionTypeIsEmpty() {
      assertThatThrownBy(
          () -> Transaction.create("Transação teste", null, BigDecimal.valueOf(10.00), LocalDate.now(),
              account, category))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class UpdateTransaction {
    @Test
    void shouldUpdateTransactionDescription() {
      Transaction transaction = Transaction.create("Transação teste", TransactionType.EXPENSE,
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          category);

      String transactionNewName = "Transação teste 2";

      transaction.update(transactionNewName, transaction.getType(), transaction.getAmount(),
          transaction.getDate(), transaction.getAccount(), transaction.getCategory());

      assertThat(transaction.getDescription()).isEqualTo(transactionNewName);
    }

    @Test
    void shouldUpdateTransactionTypeAndFlipSignedAmount() {
      Transaction transaction = Transaction.create("Transação teste", TransactionType.INCOME,
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          category);

      transaction.update(transaction.getDescription(), TransactionType.EXPENSE, transaction.getAmount(),
          transaction.getDate(), transaction.getAccount(), transaction.getCategory());

      assertThat(transaction.getType()).isEqualTo(TransactionType.EXPENSE);
      assertThat(transaction.getSignedAmount()).isNegative();
    }
  }

  @Nested
  class Amount {
    @Test
    void shouldReturnProperSignedAmountForIncomeTransaction() {
      Transaction transaction = Transaction.create("Transação Income", TransactionType.INCOME,
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          category);

      assertThat(transaction.getSignedAmount()).isPositive();
    }

    @Test
    void shouldReturnProperSignedAmountForExpenseTransaction() {
      Transaction transaction = Transaction.create("Transação Expense", TransactionType.EXPENSE,
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          category);

      assertThat(transaction.getSignedAmount()).isNegative();
    }
  }
}

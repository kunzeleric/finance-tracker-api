package com.kunzel.finance_tracker.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.account.AccountTestFixtures;
import com.kunzel.finance_tracker.category.CategoryTestFixtures;

public class TransactionTest {
  List<Transaction> transactions;
  Category category;
  Account account;

  @BeforeEach
  void setup() {
    category = CategoryTestFixtures.defaultIncomeCategory();
    account = AccountTestFixtures.defaultAccount();
  }

  @Nested
  class Constructor {
    @Test
    void shouldCreateTransaction() {
      Transaction transaction = Transaction.create("Transação teste", new BigDecimal(100.00), LocalDate.now(), account,
          category);

      assertEquals(BigDecimal.valueOf(100), transaction.getAmount());
      assertEquals(account, transaction.getAccount());
      assertEquals(category, transaction.getCategory());
    }

    @Test
    void shouldThrowExceptionWhenTransactionValueIsZero() {
      assertThatThrownBy(
          () -> Transaction.create("Transação teste", new BigDecimal(0.00), LocalDate.now(), account, category))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenTransactionDateIsEmpty() {
      assertThatThrownBy(() -> Transaction.create("Transação teste", new BigDecimal(10.00), null, account, category))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class UpdateTransaction {
    @Test
    void shouldUpdateTransactionDescription() {
      Transaction transaction = Transaction.create("Transação teste", new BigDecimal(100.00), LocalDate.now(), account,
          category);

      String transactionNewName = "Transação teste 2";

      transaction.updateDetails(transactionNewName, transaction.getAmount(), transaction.getDate(),
          transaction.getAccount(), transaction.getCategory());

      assertThat(transaction.getDescription()).isEqualTo(transactionNewName);
    }
  }
}

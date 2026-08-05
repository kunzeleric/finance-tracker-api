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
import com.kunzel.finance_tracker.account.AccountTestFixtures;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryTestFixtures;
import com.kunzel.finance_tracker.category.CategoryType;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

public class TransactionTest {
  Category expenseCategory;
  Category incomeCategory;
  Account account;

  @BeforeEach
  void setup() {
    expenseCategory = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);
    incomeCategory = CategoryTestFixtures.withId(2L, "Salário", CategoryType.INCOME);
    account = AccountTestFixtures.defaultAccount();
  }

  @Nested
  class Constructor {
    @Test
    void shouldCreateTransaction() {
      Transaction transaction = Transaction.create("Transação teste",
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          expenseCategory);

      assertEquals(BigDecimal.valueOf(100.00), transaction.getAmount());
      assertEquals(account, transaction.getAccount());
      assertEquals(expenseCategory, transaction.getCategory());
    }

    @Test
    void shouldThrowExceptionWhenTransactionValueIsZero() {
      assertThatThrownBy(
          () -> Transaction.create("Transação teste", BigDecimal.valueOf(0.00),
              LocalDate.now(), account, expenseCategory))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenTransactionDateIsEmpty() {
      assertThatThrownBy(
          () -> Transaction.create("Transação teste", BigDecimal.valueOf(10.00), null,
              account, expenseCategory))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenCategoryIsEmpty() {
      assertThatThrownBy(
          () -> Transaction.create("Transação teste", BigDecimal.valueOf(10.00), LocalDate.now(),
              account, null))
          .isInstanceOf(ValidationException.class);
    }
  }

  @Nested
  class DerivedType {
    @Test
    void shouldDeriveTypeFromCategory() {
      Transaction expense = Transaction.create("Mercado", BigDecimal.valueOf(100.00), LocalDate.now(),
          account, expenseCategory);
      Transaction income = Transaction.create("Salário", BigDecimal.valueOf(100.00), LocalDate.now(),
          account, incomeCategory);

      assertThat(expense.getType()).isEqualTo(CategoryType.EXPENSE);
      assertThat(income.getType()).isEqualTo(CategoryType.INCOME);
    }

    @Test
    void shouldFollowCategoryWhenCategoryTypeChanges() {
      Category category = CategoryTestFixtures.withId(1L, "Freelance", CategoryType.INCOME);
      Transaction transaction = Transaction.create("Projeto", BigDecimal.valueOf(100.00), LocalDate.now(),
          account, category);

      category.update(null, null, CategoryType.EXPENSE);

      assertThat(transaction.getType()).isEqualTo(CategoryType.EXPENSE);
      assertThat(transaction.getSignedAmount()).isNegative();
    }
  }

  @Nested
  class UpdateTransaction {
    @Test
    void shouldUpdateTransactionDescription() {
      Transaction transaction = Transaction.create("Transação teste",
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          expenseCategory);

      String transactionNewName = "Transação teste 2";

      transaction.update(transactionNewName, transaction.getAmount(),
          transaction.getDate(), transaction.getAccount(), transaction.getCategory());

      assertThat(transaction.getDescription()).isEqualTo(transactionNewName);
    }

    @Test
    void shouldFlipSignedAmountWhenMovedToCategoryOfOtherType() {
      Transaction transaction = Transaction.create("Transação teste",
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          incomeCategory);

      assertThat(transaction.getSignedAmount()).isPositive();

      transaction.update(transaction.getDescription(), transaction.getAmount(),
          transaction.getDate(), transaction.getAccount(), expenseCategory);

      assertThat(transaction.getType()).isEqualTo(CategoryType.EXPENSE);
      assertThat(transaction.getSignedAmount()).isNegative();
    }
  }

  @Nested
  class Amount {
    @Test
    void shouldReturnProperSignedAmountForIncomeTransaction() {
      Transaction transaction = Transaction.create("Transação Income",
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          incomeCategory);

      assertThat(transaction.getSignedAmount()).isPositive();
    }

    @Test
    void shouldReturnProperSignedAmountForExpenseTransaction() {
      Transaction transaction = Transaction.create("Transação Expense",
          BigDecimal.valueOf(100.00), LocalDate.now(),
          account,
          expenseCategory);

      assertThat(transaction.getSignedAmount()).isNegative();
    }
  }
}

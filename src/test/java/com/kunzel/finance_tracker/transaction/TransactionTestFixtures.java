package com.kunzel.finance_tracker.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.test.util.ReflectionTestUtils;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountTestFixtures;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryTestFixtures;

public class TransactionTestFixtures {

  public static Transaction defaultTransaction() {
    return Transaction.create("Transação Teste", BigDecimal.valueOf(100.00), LocalDate.now(),
        AccountTestFixtures.defaultAccount(),
        CategoryTestFixtures.defaultIncomeCategory());
  }

  public static Transaction withId(Long id, String description, BigDecimal amount, LocalDate date, Account account,
      Category category) {
    Transaction transaction = Transaction.create(description, amount, date, account, category);
    ReflectionTestUtils.setField(transaction, "id", id);
    return transaction;
  }
}
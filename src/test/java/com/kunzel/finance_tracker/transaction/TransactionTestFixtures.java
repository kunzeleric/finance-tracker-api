package com.kunzel.finance_tracker.transaction;

import java.math.BigDecimal;
import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountType;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryType;

class TransactionTestFixtures {

  static Account defaultAccount() {
    return Account.create("Conta Teste", BigDecimal.TEN, AccountType.CHECKING);
  }

  static Category defaultCategory() {
    return Category.createCustom("Categoria Teste", CategoryType.EXPENSE);
  }
}
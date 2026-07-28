package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;

import org.springframework.test.util.ReflectionTestUtils;

class AccountTestFixtures {
  static Account defaultAccount() {
    return Account.create("Conta teste", BigDecimal.ZERO, AccountType.SAVINGS);
  }

  static Account withId(Long id, String name, BigDecimal initialBalance, AccountType type) {
    Account account = Account.create(name, initialBalance, type);
    ReflectionTestUtils.setField(account, "id", id);
    return account;
  }
}

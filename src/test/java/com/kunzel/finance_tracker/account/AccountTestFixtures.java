package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;

import org.springframework.test.util.ReflectionTestUtils;

public class AccountTestFixtures {
  public static Account defaultAccount() {
    return Account.create("Conta teste", BigDecimal.ZERO, AccountType.SAVINGS, null, null);
  }

  public static Account savingsWithId(Long id, BigDecimal openingBalance) {
    return withId(id, "Conta Teste", openingBalance, AccountType.SAVINGS);
  }

  public static Account withId(Long id, String name, BigDecimal openingBalance, AccountType type) {
    Account account = Account.create(name, openingBalance, type, null, null);
    ReflectionTestUtils.setField(account, "id", id);
    return account;
  }
}

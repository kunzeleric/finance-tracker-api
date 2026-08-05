package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;

public record AccountWithBalance(Account account, BigDecimal currentBalance) {

  public Long id() {
    return account.getId();
  }
}

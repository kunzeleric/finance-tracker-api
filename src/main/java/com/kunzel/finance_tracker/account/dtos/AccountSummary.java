package com.kunzel.finance_tracker.account.dtos;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountType;

public record AccountSummary(Long accountId, String name, AccountType type, BigDecimal currentBalance) {
  public static AccountSummary from(Account account) {
    return new AccountSummary(account.getId(), account.getName(), account.getType(), account.getCurrentBalance());
  }
}

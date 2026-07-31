package com.kunzel.finance_tracker.account.dtos;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountType;

public record AccountResponse(Long accountId, String name, AccountType type, BigDecimal initialBalance,
    BigDecimal currentBalance) {
  public static AccountResponse from(Account account) {
    return new AccountResponse(account.getId(), account.getName(), account.getType(), account.getInitialBalance(),
        account.getCurrentBalance());
  }
}

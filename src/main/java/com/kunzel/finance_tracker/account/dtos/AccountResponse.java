package com.kunzel.finance_tracker.account.dtos;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountType;

public record AccountResponse(Long accountId, String name, AccountType type, BigDecimal saldo) {
  public AccountResponse(Account account) {
    this(account.getId(), account.getName(), account.getType(), account.getBalance());
  }
}

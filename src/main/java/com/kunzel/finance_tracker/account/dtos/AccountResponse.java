package com.kunzel.finance_tracker.account.dtos;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountType;
import com.kunzel.finance_tracker.account.AccountWithBalance;

public record AccountResponse(Long accountId, String name, AccountType type, String color, String institution,
    BigDecimal openingBalance,
    BigDecimal currentBalance) {

  public static AccountResponse from(Account account, BigDecimal currentBalance) {
    return new AccountResponse(account.getId(), account.getName(), account.getType(), account.getColor(),
        account.getInstitution(), account.getOpeningBalance(), currentBalance);
  }

  public static AccountResponse from(AccountWithBalance accountWithBalance) {
    return from(accountWithBalance.account(), accountWithBalance.currentBalance());
  }
}

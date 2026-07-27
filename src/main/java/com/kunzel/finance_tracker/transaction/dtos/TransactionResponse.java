package com.kunzel.finance_tracker.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.transaction.Transaction;

public record TransactionResponse(Long transactionId, String description, BigDecimal amount, LocalDate date,
    Account account,
    Category category) {
  public TransactionResponse(Transaction transaction) {
    this(transaction.getId(), transaction.getDescription(), transaction.getAmount(), transaction.getDate(),
        transaction.getAccount(), transaction.getCategory());
  }

}

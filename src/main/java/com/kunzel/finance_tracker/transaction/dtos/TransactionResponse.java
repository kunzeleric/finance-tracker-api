package com.kunzel.finance_tracker.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kunzel.finance_tracker.account.dtos.AccountSummary;
import com.kunzel.finance_tracker.category.dtos.CategorySummary;
import com.kunzel.finance_tracker.transaction.Transaction;

public record TransactionResponse(Long transactionId, String description, BigDecimal amount, BigDecimal signedAmount,
    LocalDate date,
    AccountSummary account,
    CategorySummary category) {
  public static TransactionResponse from(Transaction transaction) {
    return new TransactionResponse(
        transaction.getId(),
        transaction.getDescription(),
        transaction.getAmount(),
        transaction.getSignedAmount(),
        transaction.getDate(),
        AccountSummary.from(transaction.getAccount()),
        CategorySummary.from(transaction.getCategory()));
  }

}

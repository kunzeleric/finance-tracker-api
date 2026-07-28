package com.kunzel.finance_tracker.transaction.dtos;

import java.time.LocalDate;

public record TransactionFilter(Long accountId, Long categoryId, LocalDate startDate, LocalDate endDate) {

  public static TransactionFilter from(Long accountId, Long categoryId, LocalDate startDate, LocalDate endDate) {
    return new TransactionFilter(accountId, categoryId, startDate, endDate);
  }
}

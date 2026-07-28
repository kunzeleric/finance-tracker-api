package com.kunzel.finance_tracker.transaction.dtos;

import java.util.List;

import com.kunzel.finance_tracker.transaction.Transaction;

public record TransactionSearchResponse(
    TransactionFilter filters, int count, List<TransactionResponse> results) {
  public static TransactionSearchResponse of(List<Transaction> transactions, TransactionFilter filters) {
    List<TransactionResponse> results = transactions.stream().map(TransactionResponse::from).toList();
    return new TransactionSearchResponse(filters, results.size(), results);
  }
}

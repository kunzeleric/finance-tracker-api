package com.kunzel.finance_tracker.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kunzel.finance_tracker.category.dtos.CategoryResponse;
import com.kunzel.finance_tracker.category.dtos.UpdateCategoryRequest;
import com.kunzel.finance_tracker.transaction.dtos.CreateTransactionRequest;
import com.kunzel.finance_tracker.transaction.dtos.TransactionFilter;
import com.kunzel.finance_tracker.transaction.dtos.TransactionResponse;
import com.kunzel.finance_tracker.transaction.dtos.TransactionSearchResponse;
import com.kunzel.finance_tracker.transaction.dtos.UpdateTransactionRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @GetMapping
  public ResponseEntity<TransactionSearchResponse> fetchTransactions(
      @RequestParam(required = false) Long accountId,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    TransactionFilter filters = TransactionFilter.from(accountId, categoryId, startDate, endDate);
    List<Transaction> transactions = transactionService.searchTransactions(accountId, categoryId, startDate, endDate);

    return ResponseEntity.ok().body(TransactionSearchResponse.of(transactions, filters));
  }

  @GetMapping("/{id}")
  public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable("id") Long transactionId) {
    return ResponseEntity.ok().body(TransactionResponse.from(transactionService.getTransactionById(transactionId)));
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
    Transaction createdTransaction = transactionService.createTransaction(request.description(), request.amount(),
        request.date(), request.accountId(), request.categoryId());
    return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(createdTransaction));
  }

  @PutMapping("/{id}")
  public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable("id") Long transactionId,
      @Valid @RequestBody UpdateTransactionRequest request) {
    Transaction updatedTransaction = transactionService.updateTransaction(transactionId, request.description(),
        request.amount(), request.date(), request.accountId(), request.categoryId());
    return ResponseEntity.ok().body(TransactionResponse.from(updatedTransaction));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> removeTransaction(@PathVariable("id") Long transactionId) {
    transactionService.removeTransaction(transactionId);
    return ResponseEntity.noContent().build();
  }
}

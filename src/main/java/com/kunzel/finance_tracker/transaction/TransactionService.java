package com.kunzel.finance_tracker.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;

@Service
public class TransactionService {
  private final TransactionRepository transactionRepository;

  public TransactionService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public List<Transaction> getAllTransactions() {
    return transactionRepository.findAll();
  }

  public Transaction getTransactionById(Long transactionId) {
    return transactionRepository.findById(transactionId)
        .orElseThrow(() -> new NotFoundException(transactionId, "TRANSAÇÃO"));
  }

  public Transaction createTransaction(String description, BigDecimal amount, LocalDate date, Account account,
      Category category) {
    return transactionRepository.save(Transaction.create(description, amount, date, account, category));
  }

  public Transaction updateTransaction(Long transactionId, String description, BigDecimal amount, LocalDate date,
      Account account,
      Category category) {
    Transaction transactionToUpdate = getTransactionById(transactionId);
    transactionToUpdate.updateDetails(description, amount, date, account, category);
    return transactionToUpdate;
  }

  public void removeTransaction(Long transactionId) {
    Transaction transactionToRemove = getTransactionById(transactionId);
    transactionRepository.delete(transactionToRemove);
  }

}

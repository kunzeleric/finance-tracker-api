package com.kunzel.finance_tracker.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountRepository;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryRepository;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;

import jakarta.transaction.Transactional;

@Service
public class TransactionService {
  private final TransactionRepository transactionRepository;
  private final CategoryRepository categoryRepository;
  private final AccountRepository accountRepository;

  public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
      AccountRepository accountRepository) {
    this.transactionRepository = transactionRepository;
    this.categoryRepository = categoryRepository;
    this.accountRepository = accountRepository;
  }

  public List<Transaction> getAllTransactions() {
    return transactionRepository.findAll();
  }

  public Transaction getTransactionById(Long transactionId) {
    return transactionRepository.findById(transactionId)
        .orElseThrow(() -> new NotFoundException(transactionId, "TRANSAÇÃO"));
  }

  public Transaction createTransaction(String description, BigDecimal amount, LocalDate date, Long accountId,
      Long categoryId) {

    Account account = findAccountById(accountId);
    Category category = findCategoryById(categoryId);

    return transactionRepository.save(Transaction.create(description, amount, date, account, category));
  }

  @Transactional
  public Transaction updateTransaction(Long transactionId, String description, BigDecimal amount, LocalDate date,
      Long accountId,
      Long categoryId) {
    Account account = findAccountById(accountId);
    Category category = findCategoryById(categoryId);

    Transaction transactionToUpdate = getTransactionById(transactionId);
    transactionToUpdate.updateDetails(description, amount, date, account, category);
    return transactionToUpdate;
  }

  public void removeTransaction(Long transactionId) {
    Transaction transactionToRemove = getTransactionById(transactionId);
    transactionRepository.delete(transactionToRemove);
  }

  private Account findAccountById(Long accountId) {
    return accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException(accountId, "CONTA"));
  }

  private Category findCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new NotFoundException(categoryId, "CATEGORIA"));
  }

}

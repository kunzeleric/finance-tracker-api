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

import org.springframework.transaction.annotation.Transactional;

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

  public List<Transaction> searchTransactions(Long accountId, Long categoryId, LocalDate startDate, LocalDate endDate) {
    return transactionRepository.search(accountId, categoryId, startDate, endDate);
  }

  public Transaction getTransactionById(Long transactionId) {
    return transactionRepository.findByIdWithRelations(transactionId)
        .orElseThrow(() -> new NotFoundException(transactionId, "TRANSAÇÃO"));
  }

  @Transactional
  public Transaction createTransaction(String description, TransactionType type, BigDecimal amount, LocalDate date,
      Long accountId,
      Long categoryId) {

    Account account = findAccountById(accountId);
    Category category = findCategoryById(categoryId);

    Transaction createdTransaction = Transaction.create(description, type, amount, date, account, category);
    transactionRepository.save(createdTransaction);

    account.applyTransaction(createdTransaction.getSignedAmount());
    accountRepository.save(account);

    return createdTransaction;
  }

  @Transactional
  public Transaction updateTransaction(Long transactionId, String description, TransactionType type, BigDecimal amount,
      LocalDate date,
      Long accountId, Long categoryId) {

    Transaction transactionToUpdate = getTransactionById(transactionId);

    Account oldAccount = transactionToUpdate.getAccount();
    BigDecimal oldSignedAmount = transactionToUpdate.getSignedAmount();

    Account newAccount = findAccountById(accountId);
    Category newCategory = findCategoryById(categoryId);

    transactionToUpdate.update(description, type, amount, date, newAccount, newCategory);

    oldAccount.reverseTransaction(oldSignedAmount);
    newAccount.applyTransaction(transactionToUpdate.getSignedAmount());

    accountRepository.save(oldAccount);
    accountRepository.save(newAccount);

    return transactionRepository.save(transactionToUpdate);
  }

  @Transactional
  public void removeTransaction(Long transactionId) {
    Transaction transactionToRemove = getTransactionById(transactionId);

    Account account = transactionToRemove.getAccount();
    account.reverseTransaction(transactionToRemove.getSignedAmount());
    accountRepository.save(account);

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

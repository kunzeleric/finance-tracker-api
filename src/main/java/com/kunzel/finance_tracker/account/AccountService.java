package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;

@Service
public class AccountService {
  private final AccountRepository accountRepository;

  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public List<Account> getAllAccounts() {
    return accountRepository.findAll();
  }

  public Account getAccountById(Long accountId) {
    return accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException(accountId, "CONTA"));
  }

  public Account createAccount(String name, BigDecimal initialBalance, AccountType type) {
    return accountRepository.save(Account.create(name, initialBalance, type));
  }

  public Account updateAccount(Long accountId, String name, AccountType type) {
    Account accountToUpdate = getAccountById(accountId);
    accountToUpdate.updateDetails(name, type);
    return accountRepository.save(accountToUpdate);
  }

  public void removeAccount(Long accountId) {
    Account found = getAccountById(accountId);
    accountRepository.delete(found);

    // TODO: cascade delete de TRANSACTIONS ligadas à conta removida (account_id)
  }

  public BigDecimal getTotalBalance() {
    return accountRepository.findAll().stream().map(Account::getCurrentBalance).reduce(BigDecimal.ZERO,
        BigDecimal::add);
  }

}

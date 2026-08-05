package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kunzel.finance_tracker.category.CategoryType;
import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;
import com.kunzel.finance_tracker.transaction.AccountSignedSum;
import com.kunzel.finance_tracker.transaction.TransactionRepository;

@Service
public class AccountService {
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  public List<AccountWithBalance> getAllAccountsWithBalance() {
    Map<Long, BigDecimal> signedSums = transactionRepository.signedSumGroupedByAccount(CategoryType.EXPENSE).stream()
        .collect(Collectors.toMap(AccountSignedSum::accountId, AccountSignedSum::signedSum));

    return accountRepository.findAll().stream()
        .map(account -> new AccountWithBalance(account, balanceOf(account, signedSums)))
        .toList();
  }

  public AccountWithBalance getAccountWithBalance(Long accountId) {
    Account account = getAccountById(accountId);
    return new AccountWithBalance(account,
        account.getOpeningBalance().add(transactionRepository.signedSum(accountId, CategoryType.EXPENSE)));
  }

  private Account getAccountById(Long accountId) {
    return accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException(accountId, "CONTA"));
  }

  public AccountWithBalance createAccount(String name, BigDecimal openingBalance, AccountType type, String color,
      String institution) {
    assertNameTypeAvailable(name, type, null);
    Account created = accountRepository.save(Account.create(name, openingBalance, type, color, institution));

    return new AccountWithBalance(created, created.getOpeningBalance());
  }

  public AccountWithBalance updateAccount(Long accountId, String name, AccountType type, String color,
      String institution) {
    Account accountToUpdate = getAccountById(accountId);
    assertNameTypeAvailable(name, type, accountId);

    accountToUpdate.update(name, type, color, institution);
    Account updated = accountRepository.save(accountToUpdate);

    return new AccountWithBalance(updated,
        updated.getOpeningBalance().add(transactionRepository.signedSum(accountId, CategoryType.EXPENSE)));
  }

  @Transactional
  public void removeAccount(Long accountId) {
    Account found = getAccountById(accountId);

    transactionRepository.deleteByAccountId(accountId);
    accountRepository.delete(found);
  }

  public BigDecimal getTotalBalance() {
    return accountRepository.sumOpeningBalances().add(transactionRepository.signedSum(null, CategoryType.EXPENSE));
  }

  private BigDecimal balanceOf(Account account, Map<Long, BigDecimal> signedSums) {
    return account.getOpeningBalance().add(signedSums.getOrDefault(account.getId(), BigDecimal.ZERO));
  }

  private void assertNameTypeAvailable(String name, AccountType type, Long excludeId) {
    if (name == null || type == null) {
      return;
    }

    accountRepository.findExistingAccountByNameAndType(name, type)
        .filter(existing -> !existing.getId().equals(excludeId))
        .ifPresent(existing -> {
          throw new BusinessRuleException("Você não pode ter duas contas com mesmo nome e tipo");
        });
  }

}

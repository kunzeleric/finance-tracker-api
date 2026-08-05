package com.kunzel.finance_tracker.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;
import com.kunzel.finance_tracker.category.CategoryType;
import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;
import com.kunzel.finance_tracker.transaction.AccountSignedSum;
import com.kunzel.finance_tracker.transaction.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
  @Mock
  private AccountRepository accountRepository;

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private AccountService accountService;

  private void givenAccountExists(Account account) {
    when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
  }

  private void givenAccountSaveReturnsArgument() {
    when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  private void givenNameAndTypeAvailable(String name, AccountType type) {
    when(accountRepository.findExistingAccountByNameAndType(name, type)).thenReturn(Optional.empty());
  }

  private void givenNameAndTypeTakenBy(Account accountOwner) {
    when(accountRepository.findExistingAccountByNameAndType(accountOwner.getName(), accountOwner.getType()))
        .thenReturn(Optional.of(accountOwner));
  }

  private void givenSignedSumForAccount(Long accountId, BigDecimal signedSum) {
    when(transactionRepository.signedSum(accountId, CategoryType.EXPENSE)).thenReturn(signedSum);
  }

  private void givenSignedSumsByAccount(AccountSignedSum... rows) {
    when(transactionRepository.signedSumGroupedByAccount(CategoryType.EXPENSE)).thenReturn(List.of(rows));
  }

  @Nested
  class Create {
    @Test
    void shouldCreateAccount() {
      Account account = AccountTestFixtures.defaultAccount();

      givenAccountSaveReturnsArgument();

      AccountWithBalance createdAccount = accountService.createAccount(account.getName(),
          account.getOpeningBalance(), account.getType(), "#7C3AED", "Nubank");

      assertThat(createdAccount.account().getName()).isEqualTo(account.getName());
      assertThat(createdAccount.account().getColor()).isEqualTo("#7C3AED");
      assertThat(createdAccount.account().getInstitution()).isEqualTo("Nubank");

      verify(accountRepository).save(createdAccount.account());
    }

    @Test
    void shouldStartCurrentBalanceAtOpeningBalance() {
      givenAccountSaveReturnsArgument();

      AccountWithBalance createdAccount = accountService.createAccount("Conta Nova", BigDecimal.valueOf(250.00),
          AccountType.CHECKING, null, null);

      assertThat(createdAccount.currentBalance()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
    }

    @Test
    void shouldThrowExceptionWhenCreatingAccountWithNegativeBalance() {
      assertThatThrownBy(
          () -> accountService.createAccount("Conta Poupança Nubank", BigDecimal.valueOf(-100.00),
              AccountType.SAVINGS, null, null))
          .isInstanceOf(InvalidBalanceException.class);

      verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAccountWithDuplicatedNameAndType() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Poupança Nubank", BigDecimal.valueOf(1000.00),
          AccountType.SAVINGS);

      givenNameAndTypeTakenBy(existingAccount);

      assertThatThrownBy(
          () -> accountService.createAccount("Conta Poupança Nubank", BigDecimal.valueOf(100.00),
              AccountType.SAVINGS, null, null))
          .isInstanceOf(BusinessRuleException.class);

      verify(accountRepository, never()).save(any());
    }
  }

  @Nested
  class Get {

    @Test
    void shouldReturnAllAccountsWithDerivedBalance() {
      Account account1 = AccountTestFixtures.withId(1L, "Conta 1", BigDecimal.valueOf(100.00), AccountType.SAVINGS);
      Account account2 = AccountTestFixtures.withId(2L, "Conta 2", BigDecimal.valueOf(300.00), AccountType.WALLET);

      when(accountRepository.findAll()).thenReturn(List.of(account1, account2));
      givenSignedSumsByAccount(new AccountSignedSum(1L, BigDecimal.valueOf(50.00)));

      List<AccountWithBalance> accounts = accountService.getAllAccountsWithBalance();

      assertThat(accounts).hasSize(2);
      // conta 1 tem lançamentos; conta 2 não aparece na agregação e cai no default.
      assertThat(accounts.get(0).currentBalance()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
      assertThat(accounts.get(1).currentBalance()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() {
      when(accountRepository.findAll()).thenReturn(List.of());
      givenSignedSumsByAccount();

      assertThat(accountService.getAllAccountsWithBalance()).isEmpty();
    }

    @Test
    void shouldGetAccountWithValidId() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.SAVINGS);

      givenAccountExists(existingAccount);
      givenSignedSumForAccount(1L, BigDecimal.ZERO);

      AccountWithBalance found = accountService.getAccountWithBalance(existingAccount.getId());

      assertThat(found.account()).isEqualTo(existingAccount);
      assertThat(found.id()).isEqualTo(existingAccount.getId());

      verify(accountRepository).findById(existingAccount.getId());
    }

    @Test
    void shouldDeriveBalanceFromOpeningBalancePlusSignedSum() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.SAVINGS);

      givenAccountExists(existingAccount);
      givenSignedSumForAccount(1L, BigDecimal.valueOf(-30.00));

      AccountWithBalance found = accountService.getAccountWithBalance(1L);

      assertThat(found.currentBalance()).isEqualByComparingTo(BigDecimal.valueOf(70.00));
    }

    @Test
    void shouldThrowExceptionWhenGettingAccountWithInvalidId() {
      Long invalidId = 2L;

      when(accountRepository.findById(invalidId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> accountService.getAccountWithBalance(invalidId))
          .isInstanceOf(NotFoundException.class);

      verify(accountRepository).findById(invalidId);
    }
  }

  @Nested
  class Update {

    @Test
    void shouldUpdateAccountWithValidName() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.SAVINGS);
      String newAccountName = "Conta Nova";

      givenAccountExists(existingAccount);
      givenNameAndTypeAvailable(newAccountName, existingAccount.getType());
      givenAccountSaveReturnsArgument();
      givenSignedSumForAccount(1L, BigDecimal.ZERO);

      AccountWithBalance updatedAccount = accountService.updateAccount(existingAccount.getId(), newAccountName,
          AccountType.SAVINGS, null, null);

      assertThat(updatedAccount.account().getName()).isEqualTo(newAccountName);
      assertThat(updatedAccount.account().getId()).isEqualTo(existingAccount.getId());

      verify(accountRepository).findById(existingAccount.getId());
      verify(accountRepository).save(updatedAccount.account());
    }

    @Test
    void shouldUpdateColorAndInstitution() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.SAVINGS);

      givenAccountExists(existingAccount);
      givenNameAndTypeTakenBy(existingAccount);
      givenAccountSaveReturnsArgument();
      givenSignedSumForAccount(1L, BigDecimal.ZERO);

      AccountWithBalance updatedAccount = accountService.updateAccount(existingAccount.getId(), "Conta Teste",
          AccountType.SAVINGS, "#0EA5E9", "Inter");

      assertThat(updatedAccount.account().getColor()).isEqualTo("#0EA5E9");
      assertThat(updatedAccount.account().getInstitution()).isEqualTo("Inter");
    }

    @Test
    void shouldKeepOpeningBalanceUntouchedOnUpdate() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.SAVINGS);

      givenAccountExists(existingAccount);
      givenNameAndTypeTakenBy(existingAccount);
      givenAccountSaveReturnsArgument();
      givenSignedSumForAccount(1L, BigDecimal.valueOf(25.00));

      AccountWithBalance updatedAccount = accountService.updateAccount(existingAccount.getId(), "Conta Teste",
          AccountType.SAVINGS, null, null);

      assertThat(updatedAccount.account().getOpeningBalance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
      assertThat(updatedAccount.currentBalance()).isEqualByComparingTo(BigDecimal.valueOf(125.00));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingAccountWithExistingNameAndType() {
      Account regularAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.SAVINGS);
      Account accountToBeUpdated = AccountTestFixtures.withId(2L, "Conta Teste 2", BigDecimal.valueOf(100.00),
          AccountType.CHECKING);

      givenAccountExists(accountToBeUpdated);
      givenNameAndTypeTakenBy(regularAccount);

      assertThatThrownBy(
          () -> accountService.updateAccount(accountToBeUpdated.getId(), "Conta Teste", AccountType.SAVINGS, null,
              null))
          .isInstanceOf(BusinessRuleException.class);

      verify(accountRepository).findById(accountToBeUpdated.getId());
      verify(accountRepository, never()).save(any());
    }
  }

  @Nested
  class Delete {

    @Test
    void shouldCascadeDeleteTransactionsWhenRemovingAccount() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.INVESTMENT);

      givenAccountExists(existingAccount);

      accountService.removeAccount(existingAccount.getId());

      verify(transactionRepository).deleteByAccountId(existingAccount.getId());
      verify(accountRepository).delete(existingAccount);
    }

    @Test
    void shouldThrowExceptionWhenRemovingInexistentAccount() {
      Long inexistentAccountId = 2L;

      when(accountRepository.findById(inexistentAccountId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> accountService.removeAccount(inexistentAccountId)).isInstanceOf(NotFoundException.class);

      verify(accountRepository, never()).delete(any());
      verify(transactionRepository, never()).deleteByAccountId(anyLong());
    }
  }

  @Nested
  class Balance {
    @Test
    void shouldReturnAccountsTotalBalance() {
      when(accountRepository.sumOpeningBalances()).thenReturn(BigDecimal.valueOf(400.00));
      givenSignedSumForAccount(null, BigDecimal.valueOf(-150.00));

      assertThat(accountService.getTotalBalance()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
    }

    @Test
    void shouldReturnZeroWhenNoAccountsExist() {
      when(accountRepository.sumOpeningBalances()).thenReturn(BigDecimal.ZERO);
      givenSignedSumForAccount(null, BigDecimal.ZERO);

      assertThat(accountService.getTotalBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
  }

  @Nested
  class QueryCount {
    @Test
    void shouldAggregateBalancesWithASingleQueryForTheWholeList() {
      when(accountRepository.findAll()).thenReturn(List.of(
          AccountTestFixtures.withId(1L, "Conta 1", BigDecimal.ZERO, AccountType.SAVINGS),
          AccountTestFixtures.withId(2L, "Conta 2", BigDecimal.ZERO, AccountType.WALLET),
          AccountTestFixtures.withId(3L, "Conta 3", BigDecimal.ZERO, AccountType.CHECKING)));
      givenSignedSumsByAccount();

      accountService.getAllAccountsWithBalance();

      verify(transactionRepository).signedSumGroupedByAccount(CategoryType.EXPENSE);
      verify(transactionRepository, never()).signedSum(anyLong(), any());
    }
  }
}

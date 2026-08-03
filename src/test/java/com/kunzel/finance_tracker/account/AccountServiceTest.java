package com.kunzel.finance_tracker.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;
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

  private void givenAccountHasTransactions(Long accountId) {
    when(transactionRepository.existsByAccountId(accountId)).thenReturn(true);
  }

  private void givenAccountHasNoTransactions(Long accountId) {
    when(transactionRepository.existsByAccountId(accountId)).thenReturn(false);
  }

  @Nested
  class Create {
    @Test
    void shouldCreateAccount() {
      Account account = AccountTestFixtures.defaultAccount();

      givenAccountSaveReturnsArgument();

      Account createdAccount = accountService.createAccount(account.getName(), account.getInitialBalance(),
          account.getType());

      assertThat(createdAccount.getName()).isEqualTo(account.getName());

      verify(accountRepository).save(createdAccount);
    }

    @Test
    void shouldThrowExceptionWhenCreatingAccountWithNegativeBalance() {
      assertThatThrownBy(
          () -> accountService.createAccount("Conta Poupança Nubank", BigDecimal.valueOf(-100.00), AccountType.SAVINGS))
          .isInstanceOf(InvalidBalanceException.class);

      verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAccountWithDuplicatedNameAndType() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Poupança Nubank", BigDecimal.valueOf(1000.00),
          AccountType.SAVINGS);

      givenNameAndTypeTakenBy(existingAccount);

      assertThatThrownBy(
          () -> accountService.createAccount("Conta Poupança Nubank", BigDecimal.valueOf(100.00), AccountType.SAVINGS))
          .isInstanceOf(BusinessRuleException.class);

      verify(accountRepository, never()).save(any());
    }
  }

  @Nested
  class Get {

    @Test
    void shouldReturnAllAccounts() {
      Account account1 = AccountTestFixtures.withId(1L, "Conta 1", BigDecimal.valueOf(100.00), AccountType.SAVINGS);
      Account account2 = AccountTestFixtures.withId(2L, "Conta 2", BigDecimal.valueOf(300.00), AccountType.WALLET);

      when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

      List<Account> accounts = accountService.getAllAccounts();

      assertThat(accounts).hasSize(2);
      assertThat(accounts).containsExactly(account1, account2);
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() {

      when(accountRepository.findAll()).thenReturn(List.of());

      List<Account> accounts = accountService.getAllAccounts();

      assertThat(accounts).isEmpty();
    }

    @Test
    void shouldGetAccountWithValidId() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.SAVINGS);

      givenAccountExists(existingAccount);

      Account foundAccount = accountService.getAccountById(existingAccount.getId());

      assertThat(foundAccount).isEqualTo(existingAccount);

      verify(accountRepository).findById(foundAccount.getId());
    }

    @Test
    void shouldThrowExceptionWhenGettingAccountWithInvalidId() {
      Long invalidId = 2L;

      when(accountRepository.findById(invalidId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> accountService.getAccountById(invalidId)).isInstanceOf(NotFoundException.class);

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

      Account updatedAccount = accountService.updateAccount(existingAccount.getId(), newAccountName,
          AccountType.SAVINGS);

      assertThat(updatedAccount.getName()).isEqualTo(newAccountName);
      assertThat(updatedAccount.getId()).isEqualTo(existingAccount.getId());

      verify(accountRepository).findById(updatedAccount.getId());
      verify(accountRepository).save(updatedAccount);
    }

    @Test
    void shouldUpdateAccountWhenNameAndTypeBelongToItself() {
      Account accountToBeUpdated = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.SAVINGS);

      givenAccountExists(accountToBeUpdated);
      givenNameAndTypeTakenBy(accountToBeUpdated);
      givenAccountSaveReturnsArgument();

      Account sameAccountUpdated = accountService.updateAccount(accountToBeUpdated.getId(), "Conta Teste",
          AccountType.SAVINGS);

      assertThat(sameAccountUpdated).isEqualTo(accountToBeUpdated);

      verify(accountRepository).findById(accountToBeUpdated.getId());
      verify(accountRepository).save(sameAccountUpdated);
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
          () -> accountService.updateAccount(accountToBeUpdated.getId(), "Conta Teste", AccountType.SAVINGS))
          .isInstanceOf(BusinessRuleException.class);

      verify(accountRepository).findById(accountToBeUpdated.getId());
      verify(accountRepository, never()).save(any());

    }
  }

  @Nested
  class Delete {

    @Test
    void shouldRemoveExistingAccount() {
      Account existingAccount = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.INVESTMENT);

      givenAccountExists(existingAccount);
      givenAccountHasNoTransactions(existingAccount.getId());

      accountService.removeAccount(existingAccount.getId());

      verify(accountRepository).findById(existingAccount.getId());
      verify(accountRepository).delete(existingAccount);
    }

    @Test
    void shouldThrowExceptionWhenRemovingAccountWithTransactions() {
      Account accountWithTransactions = AccountTestFixtures.withId(1L, "Conta Teste", BigDecimal.valueOf(100.00),
          AccountType.INVESTMENT);

      givenAccountExists(accountWithTransactions);
      givenAccountHasTransactions(1L);

      assertThatThrownBy(() -> accountService.removeAccount(1L))
          .isInstanceOf(BusinessRuleException.class);

      verify(accountRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenRemovingInexistentAccount() {
      Long inexistentAccountId = 2L;

      when(accountRepository.findById(inexistentAccountId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> accountService.removeAccount(inexistentAccountId)).isInstanceOf(NotFoundException.class);

      verify(accountRepository, never()).delete(any());
    }
  }

  @Nested
  class Balance {
    @Test
    void shouldReturnAccountsTotalBalance() {
      Account account1 = AccountTestFixtures.withId(1L, "Conta 1", BigDecimal.valueOf(100.00), AccountType.SAVINGS);
      Account account2 = AccountTestFixtures.withId(2L, "Conta 2", BigDecimal.valueOf(300.00), AccountType.WALLET);

      when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

      BigDecimal totalBalance = accountService.getTotalBalance();

      assertThat(totalBalance).isEqualByComparingTo(account1.getCurrentBalance().add(account2.getCurrentBalance()));
    }

    @Test
    void shouldReturnZeroWhenNoAccountsExist() {
      when(accountRepository.findAll()).thenReturn(List.of());
      assertThat(accountService.getTotalBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
  }
}

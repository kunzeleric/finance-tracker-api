package com.kunzel.finance_tracker.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountRepository;
import com.kunzel.finance_tracker.account.AccountTestFixtures;
import com.kunzel.finance_tracker.account.AccountType;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryRepository;
import com.kunzel.finance_tracker.category.CategoryTestFixtures;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

        @Mock
        private TransactionRepository transactionRepository;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @InjectMocks
        private TransactionService transactionService;

        private void givenAccountExists(Account account) {
                when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        }

        private void givenCategoryExists(Category category) {
                when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        }

        private void givenTransactionExists(Transaction transaction) {
                when(transactionRepository.findByIdWithRelations(transaction.getId()))
                                .thenReturn(Optional.of(transaction));
        }

        private void givenTransactionSaveReturnsArgument() {
                when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        }

        @Nested
        class Get {

                @Test
                void shouldReturnAllTransactions() {
                        Transaction transaction1 = TransactionTestFixtures.withId(1L, "Compra 1",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(10.00),
                                        LocalDate.of(2026, 7, 27), AccountTestFixtures.defaultAccount(),
                                        CategoryTestFixtures.customCategory());
                        Transaction transaction2 = TransactionTestFixtures.withId(2L, "Compra 2",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(30.00),
                                        LocalDate.of(2026, 7, 27), AccountTestFixtures.defaultAccount(),
                                        CategoryTestFixtures.customCategory());

                        when(transactionRepository.search(null, null, null, null))
                                        .thenReturn(List.of(transaction1, transaction2));

                        List<Transaction> transactions = transactionService.searchTransactions(null, null, null, null);

                        assertThat(transactions).hasSize(2);
                        assertThat(transactions).containsExactly(transaction1, transaction2);
                }

                @Test
                void shouldCallRepositoryWithGivenFilters() {
                        Long accountId = 5L;
                        Long categoryId = 1L;
                        LocalDate start = LocalDate.of(2026, 1, 1);
                        LocalDate end = LocalDate.of(2026, 1, 31);

                        when(transactionRepository.search(accountId, categoryId, start, end)).thenReturn(List.of());

                        transactionService.searchTransactions(accountId, categoryId, start, end);

                        verify(transactionRepository).search(accountId, categoryId, start, end);
                }

                @Test
                void shouldGetTransactionWithValidId() {
                        Transaction existing = TransactionTestFixtures.withId(1L, "Mercado",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 27),
                                        AccountTestFixtures.defaultAccount(),
                                        CategoryTestFixtures.customCategory());

                        givenTransactionExists(existing);

                        Transaction found = transactionService.getTransactionById(existing.getId());

                        assertThat(found).isEqualTo(existing);
                        verify(transactionRepository).findByIdWithRelations(existing.getId());
                }

                @Test
                void shouldThrowExceptionWhenGettingTransactionWithInvalidId() {
                        Long invalidId = 2L;

                        when(transactionRepository.findByIdWithRelations(invalidId)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> transactionService.getTransactionById(invalidId))
                                        .isInstanceOf(NotFoundException.class);

                        verify(transactionRepository).findByIdWithRelations(invalidId);
                }
        }

        @Nested
        class Create {
                @Test
                void shouldCreateTransactionWithValidParameters() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        Transaction createdTransaction = transactionService.createTransaction("Transação Teste",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account.getId(),
                                        category.getId());

                        assertThat(createdTransaction.getDescription()).isEqualTo("Transação Teste");
                        assertThat(createdTransaction.getType()).isEqualTo(TransactionType.EXPENSE);
                        assertThat(createdTransaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
                        assertThat(createdTransaction.getDate()).isEqualTo(LocalDate.of(2026, 7, 28));

                        verify(transactionRepository).save(createdTransaction);
                }

                @Test
                void shouldIncreaseAccountBalanceWhenCreatingIncomeTransaction() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        transactionService.createTransaction("Transação Income",
                                        TransactionType.INCOME, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account.getId(),
                                        category.getId());

                        assertThat(account.getCurrentBalance())
                                        .isEqualByComparingTo(
                                                        account.getInitialBalance().add(BigDecimal.valueOf(100.00)));
                }

                @Test
                void shouldDecreaseAccountBalanceWhenCreatingExpenseTransaction() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        transactionService.createTransaction("Transação Expense",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account.getId(),
                                        category.getId());

                        assertThat(account.getCurrentBalance())
                                        .isEqualByComparingTo(account.getInitialBalance()
                                                        .subtract(BigDecimal.valueOf(100.00)));
                }

                @Test
                void shouldThrowExceptionWhenCreatingTransactionWithInexistentAccount() {
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");
                        Long invalidAccountId = 2L;

                        when(accountRepository.findById(invalidAccountId)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> transactionService.createTransaction("Transação Teste",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), invalidAccountId,
                                        category.getId())).isInstanceOf(NotFoundException.class);

                        verify(transactionRepository, never()).save(any());
                }

                @Test
                void shouldThrowExceptionWhenCreatingTransactionWithInexistentCategory() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Long invalidCategoryId = 2L;

                        givenAccountExists(account);
                        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> transactionService.createTransaction("Transação Teste",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account.getId(),
                                        invalidCategoryId)).isInstanceOf(NotFoundException.class);

                        verify(transactionRepository, never()).save(any());
                }
        }

        @Nested
        class Delete {
                @Test
                void shouldRemoveTransactionWithValidId() {
                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28),
                                        AccountTestFixtures.defaultAccount(),
                                        CategoryTestFixtures.defaultCategory());

                        givenTransactionExists(existingTransaction);

                        transactionService.removeTransaction(existingTransaction.getId());

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository).delete(existingTransaction);
                        verify(accountRepository).save(any(Account.class));
                }

                @Test
                void shouldRecalculateAccountBalanceWhenDeletingIncomeTransaction() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.defaultCategory();

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Income",
                                        TransactionType.INCOME, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account,
                                        category);

                        account.applyTransaction(existingTransaction.getSignedAmount());

                        givenTransactionExists(existingTransaction);

                        transactionService.removeTransaction(existingTransaction.getId());

                        assertThat(account.getCurrentBalance())
                                        .isEqualByComparingTo(account.getInitialBalance());

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(accountRepository).save(any(Account.class));
                }

                @Test
                void shouldRecalculateAccountBalanceWhenDeletingExpenseTransaction() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.defaultCategory();

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Expense",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account,
                                        category);

                        account.applyTransaction(existingTransaction.getSignedAmount());

                        givenTransactionExists(existingTransaction);

                        transactionService.removeTransaction(existingTransaction.getId());

                        assertThat(account.getCurrentBalance())
                                        .isEqualByComparingTo(account.getInitialBalance());

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(accountRepository).save(any(Account.class));
                }

                @Test
                void shouldThrowExceptionWhenDeletingTransactionWithInvalidId() {
                        Long invalidTransactionId = 2L;

                        when(transactionRepository.findByIdWithRelations(invalidTransactionId))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> transactionService.removeTransaction(invalidTransactionId))
                                        .isInstanceOf(NotFoundException.class);

                        verify(transactionRepository, never()).delete(any());
                }

        }

        @Nested
        class Update {

                @Test
                void shouldUpdateTransactionWithValidParameters() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account,
                                        category);

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        Transaction existingTransactionUpdated = transactionService.updateTransaction(
                                        existingTransaction.getId(),
                                        "Transação Teste Novo", TransactionType.EXPENSE, BigDecimal.valueOf(150.00),
                                        existingTransaction.getDate(),
                                        account.getId(), category.getId());

                        assertThat(existingTransactionUpdated.getDescription()).isEqualTo("Transação Teste Novo");
                        assertThat(existingTransactionUpdated.getAmount())
                                        .isEqualByComparingTo(BigDecimal.valueOf(150.00));

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository).save(existingTransactionUpdated);
                }

                @Test
                void shouldRecalculateAccountBalanceWhenUpdatingIncomeTransaction() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Income",
                                        TransactionType.INCOME, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account,
                                        category);

                        account.applyTransaction(existingTransaction.getSignedAmount());

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        transactionService.updateTransaction(existingTransaction.getId(),
                                        "Transação Teste Novo", TransactionType.INCOME, BigDecimal.valueOf(150.00),
                                        existingTransaction.getDate(),
                                        account.getId(), category.getId());

                        assertThat(account.getCurrentBalance())
                                        .isEqualByComparingTo(
                                                        account.getInitialBalance().add(BigDecimal.valueOf(150.00)));

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository).save(any(Transaction.class));
                        verify(accountRepository, times(2)).save(any(Account.class));
                }

                @Test
                void shouldRecalculateAccountBalanceWhenUpdatingExpenseTransaction() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Expense",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account,
                                        category);

                        account.applyTransaction(existingTransaction.getSignedAmount());

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        transactionService.updateTransaction(existingTransaction.getId(),
                                        "Transação Teste Novo", TransactionType.EXPENSE, BigDecimal.valueOf(150.00),
                                        existingTransaction.getDate(),
                                        account.getId(), category.getId());

                        assertThat(account.getCurrentBalance())
                                        .isEqualByComparingTo(account.getInitialBalance()
                                                        .subtract(BigDecimal.valueOf(150.00)));

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository).save(any(Transaction.class));
                        verify(accountRepository, times(2)).save(any(Account.class));
                }

                @Test
                void shouldThrowExceptionWhenUpdatingTransactionWithEmptyName() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account, category);

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);

                        assertThatThrownBy(() -> transactionService.updateTransaction(existingTransaction.getId(), "",
                                        existingTransaction.getType(), existingTransaction.getAmount(),
                                        existingTransaction.getDate(), account.getId(),
                                        category.getId()))
                                        .isInstanceOf(ValidationException.class);

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository, never()).save(any());
                }

                @Test
                void shouldThrowExceptionWhenUpdatingTransactionWithNegativeAmount() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account, category);

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);

                        assertThatThrownBy(() -> transactionService.updateTransaction(existingTransaction.getId(),
                                        "Transação Teste",
                                        existingTransaction.getType(), BigDecimal.valueOf(-100.00),
                                        existingTransaction.getDate(), account.getId(),
                                        category.getId()))
                                        .isInstanceOf(ValidationException.class);

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository, never()).save(any());
                }

                @Test
                void shouldRecalculateAccountBalanceWhenUpdatingTransactionType() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = CategoryTestFixtures.withId(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        TransactionType.INCOME, BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account, category);

                        account.applyTransaction(existingTransaction.getSignedAmount());

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        transactionService.updateTransaction(existingTransaction.getId(), "Transação Teste",
                                        TransactionType.EXPENSE, existingTransaction.getAmount(),
                                        existingTransaction.getDate(), account.getId(), category.getId());

                        assertThat(existingTransaction.getType()).isEqualTo(TransactionType.EXPENSE);
                        assertThat(account.getCurrentBalance())
                                        .isEqualByComparingTo(account.getInitialBalance()
                                                        .subtract(BigDecimal.valueOf(100.00)));
                }

        }

        @Nested
        class BalanceInvariant {

                /** saldo esperado = saldo inicial + soma dos valores com sinal. */
                private BigDecimal expectedBalance(Account account, List<Transaction> transactions) {
                        return transactions.stream()
                                        .map(Transaction::getSignedAmount)
                                        .reduce(account.getInitialBalance(), BigDecimal::add);
                }

                /** repo mockado que atribui id no save */
                private void givenTransactionSaveAssignsId() {
                        AtomicLong sequence = new AtomicLong(1);
                        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
                                Transaction transaction = inv.getArgument(0);
                                if (transaction.getId() == null) {
                                        ReflectionTestUtils.setField(transaction, "id", sequence.getAndIncrement());
                                }
                                return transaction;
                        });
                }

                @Test
                void balanceShouldMatchSumOfRemainingTransactionsAfterMixedOperations() {
                        Account account = AccountTestFixtures.withId(1L, "Conta Corrente", BigDecimal.valueOf(1000.00),
                                        AccountType.CHECKING);
                        Category salaryCategory = CategoryTestFixtures.withId(1L, "Salário");
                        Category marketCategory = CategoryTestFixtures.withId(2L, "Mercado");

                        givenAccountExists(account);
                        givenCategoryExists(salaryCategory);
                        givenCategoryExists(marketCategory);
                        givenTransactionSaveAssignsId();

                        Transaction salary = transactionService.createTransaction("Salário mensal",
                                        TransactionType.INCOME, BigDecimal.valueOf(4500.00),
                                        LocalDate.of(2026, 7, 5), 1L, 1L);
                        Transaction market = transactionService.createTransaction("Supermercado",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(350.00),
                                        LocalDate.of(2026, 7, 12), 1L, 2L);
                        Transaction rent = transactionService.createTransaction("Aluguel",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(1500.00),
                                        LocalDate.of(2026, 7, 1), 1L, 2L);

                        givenTransactionExists(market);
                        givenTransactionExists(rent);

                        transactionService.updateTransaction(market.getId(), "Reembolso mercado",
                                        TransactionType.INCOME, BigDecimal.valueOf(400.00),
                                        market.getDate(), 1L, 1L);
                        transactionService.removeTransaction(rent.getId());

                        List<Transaction> remainingTransactions = List.of(salary, market);

                        assertThat(account.getCurrentBalance())
                                        .isEqualByComparingTo(expectedBalance(account, remainingTransactions));
                }

                @Test
                void balanceShouldMatchSumOfRemainingTransactionsOnBothAccountsWhenTransactionChangesAccount() {
                        Account origin = AccountTestFixtures.withId(1L, "Conta Origem", BigDecimal.valueOf(1000.00),
                                        AccountType.CHECKING);
                        Account destination = AccountTestFixtures.withId(2L, "Conta Destino",
                                        BigDecimal.valueOf(500.00),
                                        AccountType.SAVINGS);
                        Category transportCategory = CategoryTestFixtures.withId(1L, "Transporte");

                        givenAccountExists(origin);
                        givenAccountExists(destination);
                        givenCategoryExists(transportCategory);
                        givenTransactionSaveAssignsId();

                        Transaction moved = transactionService.createTransaction("Gasolina",
                                        TransactionType.EXPENSE, BigDecimal.valueOf(200.00),
                                        LocalDate.of(2026, 7, 15), 1L, 1L);

                        givenTransactionExists(moved);

                        transactionService.updateTransaction(moved.getId(), moved.getDescription(), moved.getType(),
                                        moved.getAmount(), moved.getDate(),
                                        2L, 1L);

                        // as DUAS contas têm que fechar. Aqui reverse e apply operam em
                        // objetos diferentes, e é o único caminho onde um saldo pode ficar certo
                        // enquanto o outro fica errado.
                        assertThat(origin.getCurrentBalance())
                                        .isEqualByComparingTo(expectedBalance(origin, List.of()));
                        assertThat(destination.getCurrentBalance())
                                        .isEqualByComparingTo(expectedBalance(destination, List.of(moved)));
                }
        }
}

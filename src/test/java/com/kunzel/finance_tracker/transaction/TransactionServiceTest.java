package com.kunzel.finance_tracker.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.account.AccountRepository;
import com.kunzel.finance_tracker.account.AccountTestFixtures;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryRepository;
import com.kunzel.finance_tracker.category.CategoryTestFixtures;
import com.kunzel.finance_tracker.category.CategoryType;
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

        private Category expenseCategory(Long id, String name) {
                return CategoryTestFixtures.withId(id, name, CategoryType.EXPENSE);
        }

        private Category incomeCategory(Long id, String name) {
                return CategoryTestFixtures.withId(id, name, CategoryType.INCOME);
        }

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
                                        BigDecimal.valueOf(10.00),
                                        LocalDate.of(2026, 7, 27), AccountTestFixtures.defaultAccount(),
                                        CategoryTestFixtures.customCategory());
                        Transaction transaction2 = TransactionTestFixtures.withId(2L, "Compra 2",
                                        BigDecimal.valueOf(30.00),
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
                                        BigDecimal.valueOf(100.00),
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
                        Category category = expenseCategory(1L, "Categoria Teste");

                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        Transaction createdTransaction = transactionService.createTransaction("Transação Teste",
                                        BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account.getId(),
                                        category.getId());

                        assertThat(createdTransaction.getDescription()).isEqualTo("Transação Teste");
                        assertThat(createdTransaction.getType()).isEqualTo(CategoryType.EXPENSE);
                        assertThat(createdTransaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
                        assertThat(createdTransaction.getDate()).isEqualTo(LocalDate.of(2026, 7, 28));

                        verify(transactionRepository).save(createdTransaction);
                }

                @Test
                void shouldDeriveIncomeTypeFromCategory() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = incomeCategory(1L, "Salário");

                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        Transaction createdTransaction = transactionService.createTransaction("Salário mensal",
                                        BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account.getId(),
                                        category.getId());

                        assertThat(createdTransaction.getType()).isEqualTo(CategoryType.INCOME);
                        assertThat(createdTransaction.getSignedAmount()).isPositive();
                }

                @Test
                void shouldThrowExceptionWhenCreatingTransactionWithInexistentAccount() {
                        Category category = expenseCategory(1L, "Categoria Teste");
                        Long invalidAccountId = 2L;

                        when(accountRepository.findById(invalidAccountId)).thenReturn(Optional.empty());

                        assertThatThrownBy(() -> transactionService.createTransaction("Transação Teste",
                                        BigDecimal.valueOf(100.00),
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
                                        BigDecimal.valueOf(100.00),
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
                                        BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28),
                                        AccountTestFixtures.defaultAccount(),
                                        CategoryTestFixtures.defaultCategory());

                        givenTransactionExists(existingTransaction);

                        transactionService.removeTransaction(existingTransaction.getId());

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository).delete(existingTransaction);
                        verify(accountRepository, never()).save(any());
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
                        Category category = expenseCategory(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account,
                                        category);

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);
                        givenTransactionSaveReturnsArgument();

                        Transaction existingTransactionUpdated = transactionService.updateTransaction(
                                        existingTransaction.getId(),
                                        "Transação Teste Novo", BigDecimal.valueOf(150.00),
                                        existingTransaction.getDate(),
                                        account.getId(), category.getId());

                        assertThat(existingTransactionUpdated.getDescription()).isEqualTo("Transação Teste Novo");
                        assertThat(existingTransactionUpdated.getAmount())
                                        .isEqualByComparingTo(BigDecimal.valueOf(150.00));

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository).save(existingTransactionUpdated);
                }

                @Test
                void shouldFlipSignWhenMovingTransactionToCategoryOfOtherType() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category originCategory = incomeCategory(1L, "Salário");
                        Category targetCategory = expenseCategory(2L, "Mercado");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account, originCategory);

                        assertThat(existingTransaction.getSignedAmount()).isPositive();

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(targetCategory);
                        givenTransactionSaveReturnsArgument();

                        transactionService.updateTransaction(existingTransaction.getId(), "Transação Teste",
                                        existingTransaction.getAmount(),
                                        existingTransaction.getDate(), account.getId(), targetCategory.getId());

                        assertThat(existingTransaction.getType()).isEqualTo(CategoryType.EXPENSE);
                        assertThat(existingTransaction.getSignedAmount()).isNegative();
                }

                @Test
                void shouldThrowExceptionWhenUpdatingTransactionWithEmptyName() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = expenseCategory(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account, category);

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);

                        assertThatThrownBy(() -> transactionService.updateTransaction(existingTransaction.getId(), "",
                                        existingTransaction.getAmount(),
                                        existingTransaction.getDate(), account.getId(),
                                        category.getId()))
                                        .isInstanceOf(ValidationException.class);

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository, never()).save(any());
                }

                @Test
                void shouldThrowExceptionWhenUpdatingTransactionWithNegativeAmount() {
                        Account account = AccountTestFixtures.savingsWithId(1L, BigDecimal.valueOf(1000.00));
                        Category category = expenseCategory(1L, "Categoria Teste");

                        Transaction existingTransaction = TransactionTestFixtures.withId(1L, "Transação Teste",
                                        BigDecimal.valueOf(100.00),
                                        LocalDate.of(2026, 7, 28), account, category);

                        givenTransactionExists(existingTransaction);
                        givenAccountExists(account);
                        givenCategoryExists(category);

                        assertThatThrownBy(() -> transactionService.updateTransaction(existingTransaction.getId(),
                                        "Transação Teste",
                                        BigDecimal.valueOf(-100.00),
                                        existingTransaction.getDate(), account.getId(),
                                        category.getId()))
                                        .isInstanceOf(ValidationException.class);

                        verify(transactionRepository).findByIdWithRelations(existingTransaction.getId());
                        verify(transactionRepository, never()).save(any());
                }
        }
}

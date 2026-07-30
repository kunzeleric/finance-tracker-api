package com.kunzel.finance_tracker.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;
import com.kunzel.finance_tracker.transaction.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private CategoryService categoryService;

  private void givenCategoryExists(Category category) {
    when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
  }

  private void givenCategorySaveReturnsArgument() {
    when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  private void givenNameAndTypeAvailable(String name, CategoryType type) {
    when(categoryRepository.findExistingCategoryByNameAndType(name, type)).thenReturn(Optional.empty());
  }

  private void givenNameAndTypeTakenBy(Category categoryOwner) {
    when(categoryRepository.findExistingCategoryByNameAndType(categoryOwner.getName(), categoryOwner.getType()))
        .thenReturn(Optional.of(categoryOwner));
  }

  private void givenCategoryHasTransactions(Long categoryId) {
    when(transactionRepository.existsByCategoryId(categoryId)).thenReturn(true);
  }

  private void givenCategoryHasNoTransactions(Long categoryId) {
    when(transactionRepository.existsByCategoryId(categoryId)).thenReturn(false);
  }

  @Nested
  class Create {
    @Test
    void shouldThrowExceptionWhenCreatingCategoryWithDuplicateNameAndType() {
      // ARRANGE — repo diz que já existe categoria com esse nome+tipo.
      // Fixture com id → simula categoria já persistida (assertNameTypeAvailable lê
      // getId()).
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);

      givenNameAndTypeTakenBy(existingCategory);

      // ACT + ASSERT — service deve barrar a duplicata.
      assertThatThrownBy(() -> categoryService.createCategory("Alimentação", CategoryType.EXPENSE))
          .isInstanceOf(IllegalArgumentException.class);

      // Verifica a INTERAÇÃO: como lançou, nunca deve ter tentado salvar.
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldCreateCustomCategoryWhenNameAndTypeAreAvailable() {
      givenNameAndTypeAvailable("Natação", CategoryType.EXPENSE);

      givenCategorySaveReturnsArgument();

      Category category = categoryService.createCategory("Natação", CategoryType.EXPENSE);

      assertThat(category.getName()).isEqualTo("Natação");
      assertThat(category.getType()).isEqualTo(CategoryType.EXPENSE);
      assertThat(category.isDefault()).isFalse();

      verify(categoryRepository).save(category);
    }

    @Test
    void shouldCreateDefaultCategoryWhenNameAndTypeAreAvailable() {
      givenNameAndTypeAvailable("Salário", CategoryType.INCOME);

      givenCategorySaveReturnsArgument();

      Category category = categoryService.createDefaultCategory("Salário", CategoryType.INCOME);

      assertThat(category.getName()).isEqualTo("Salário");
      assertThat(category.getType()).isEqualTo(CategoryType.INCOME);
      assertThat(category.isDefault()).isTrue();

      verify(categoryRepository).save(category);
    }

  }

  @Nested
  class Delete {
    @Test
    void shouldRemoveExistingCategory() {
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);

      givenCategoryExists(existingCategory);
      givenCategoryHasNoTransactions(existingCategory.getId());

      categoryService.removeCategory(existingCategory.getId());

      verify(categoryRepository).findById(existingCategory.getId());
      verify(categoryRepository).delete(existingCategory);
    }

    @Test
    void shouldThrowExceptionWhenRemovingCategoryWithTransactions() {
      Category categoryWithTransactions = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);

      givenCategoryExists(categoryWithTransactions);
      givenCategoryHasTransactions(1L);

      assertThatThrownBy(() -> categoryService.removeCategory(1L))
          .isInstanceOf(IllegalStateException.class);

      verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingCategoryWithInvalidId() {
      Long inexistentCategoryId = 2L;

      when(categoryRepository.findById(inexistentCategoryId))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> categoryService.removeCategory(inexistentCategoryId))
          .isInstanceOf(NotFoundException.class);

      verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingDefaultCategory() {
      Category defaultCategory = CategoryTestFixtures.defaultWithId(1L, "Freelance", CategoryType.INCOME);

      givenCategoryExists(defaultCategory);

      assertThatThrownBy(() -> categoryService.removeCategory(defaultCategory.getId()))
          .isInstanceOf(IllegalArgumentException.class);

      verify(categoryRepository).findById(defaultCategory.getId());
      verify(categoryRepository, never()).delete(any());
    }
  }

  @Nested
  class Get {
    @Test
    void shouldReturnAllCategories() {
      Category category1 = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);
      Category category2 = CategoryTestFixtures.withId(2L, "Salário", CategoryType.INCOME);

      when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

      List<Category> categories = categoryService.getAllCategories();

      assertThat(categories).hasSize(2);
      assertThat(categories).containsExactly(category1, category2);
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesExist() {
      when(categoryRepository.findAll()).thenReturn(List.of());

      List<Category> categories = categoryService.getAllCategories();

      assertThat(categories).isEmpty();
    }

    @Test
    void shouldGetCategoryWithValidId() {
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);

      givenCategoryExists(existingCategory);

      Category foundCategory = categoryService.getCategoryById(existingCategory.getId());

      assertThat(foundCategory.getId()).isEqualByComparingTo(existingCategory.getId());

      verify(categoryRepository).findById(foundCategory.getId());
    }

    @Test
    void shouldThrowExceptionWhenGettingCategoryWithInvalidId() {
      Long invalidId = 2L;

      when(categoryRepository.findById(invalidId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> categoryService.getCategoryById(invalidId)).isInstanceOf(NotFoundException.class);

      verify(categoryRepository).findById(invalidId);
    }

  }

  @Nested
  class Update {

    @Test
    void shouldUpdateCustomCategoryWithValidName() {
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);
      String newCategoryName = "Alimentação Shopping";

      givenCategoryExists(existingCategory);
      givenNameAndTypeAvailable(newCategoryName, existingCategory.getType());
      givenCategorySaveReturnsArgument();

      Category updatedCategory = categoryService.updateCategory(existingCategory.getId(), newCategoryName,
          CategoryType.EXPENSE);

      assertThat(updatedCategory.getName()).isEqualTo(newCategoryName);
      assertThat(updatedCategory.getId()).isEqualByComparingTo(existingCategory.getId());

      verify(categoryRepository).findById(updatedCategory.getId());
      verify(categoryRepository).save(updatedCategory);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingTypeOfCategoryWithTransactions() {
      Category categoryWithTransactions = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);

      givenCategoryExists(categoryWithTransactions);
      givenCategoryHasTransactions(1L);

      assertThatThrownBy(() -> categoryService.updateCategory(1L, "Alimentação", CategoryType.INCOME))
          .isInstanceOf(IllegalArgumentException.class);

      // tipo não pode ter sido mutado antes do throw
      assertThat(categoryWithTransactions.getType()).isEqualTo(CategoryType.EXPENSE);

      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTypeOfCustomCategoryWithoutTransactions() {
      Category categoryWithoutTransactions = CategoryTestFixtures.withId(1L, "Bônus", CategoryType.EXPENSE);

      givenCategoryExists(categoryWithoutTransactions);
      givenCategoryHasNoTransactions(1L);
      givenNameAndTypeAvailable("Bônus", CategoryType.INCOME);
      givenCategorySaveReturnsArgument();

      Category updatedCategory = categoryService.updateCategory(1L, "Bônus", CategoryType.INCOME);

      assertThat(updatedCategory.getType()).isEqualTo(CategoryType.INCOME);
      assertThat(updatedCategory.getName()).isEqualTo("Bônus");

      verify(transactionRepository).existsByCategoryId(1L);
      verify(categoryRepository).save(updatedCategory);
    }

    @Test
    void shouldUpdateNameOfCustomCategoryWithTransactionsWhenTypeIsUnchanged() {
      Category categoryWithTransactions = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);

      givenCategoryExists(categoryWithTransactions);
      givenNameAndTypeAvailable("Mercado", CategoryType.EXPENSE);
      givenCategorySaveReturnsArgument();

      Category updatedCategory = categoryService.updateCategory(1L, "Mercado", CategoryType.EXPENSE);

      assertThat(updatedCategory.getName()).isEqualTo("Mercado");
      assertThat(updatedCategory.getType()).isEqualTo(CategoryType.EXPENSE);

      // tipo não mudou → guarda nem chega a consultar os lançamentos
      verify(transactionRepository, never()).existsByCategoryId(anyLong());
      verify(categoryRepository).save(updatedCategory);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingDefaultCategory() {
      Category existingDefaultCategory = CategoryTestFixtures.defaultWithId(1L, "Categoria Padrão",
          CategoryType.INCOME);

      givenCategoryExists(existingDefaultCategory);

      assertThatThrownBy(
          () -> categoryService.updateCategory(existingDefaultCategory.getId(), "Nova Categoria Padrão",
              CategoryType.EXPENSE))
          .isInstanceOf(IllegalArgumentException.class);

      verify(categoryRepository).findById(existingDefaultCategory.getId());
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingCustomCategoryWithExistingNameAndType() {
      Category regularCategory = CategoryTestFixtures.withId(1L, "Freelance", CategoryType.INCOME);
      Category categoryToBeUpdated = CategoryTestFixtures.withId(2L, "Salário", CategoryType.INCOME);

      givenCategoryExists(categoryToBeUpdated);
      givenNameAndTypeTakenBy(regularCategory);

      assertThatThrownBy(
          () -> categoryService.updateCategory(categoryToBeUpdated.getId(), "Freelance", CategoryType.INCOME))
          .isInstanceOf(IllegalArgumentException.class);

      verify(categoryRepository).findById(categoryToBeUpdated.getId());
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCustomCategoryWhenNameAndTypeBelongToItself() {
      Category categoryToBeUpdated = CategoryTestFixtures.withId(1L, "Freelance", CategoryType.INCOME);

      givenCategoryExists(categoryToBeUpdated);
      givenNameAndTypeTakenBy(categoryToBeUpdated);

      givenCategorySaveReturnsArgument();

      Category sameCategoryUpdated = categoryService.updateCategory(categoryToBeUpdated.getId(), "Freelance",
          CategoryType.INCOME);

      assertThat(sameCategoryUpdated).isEqualTo(categoryToBeUpdated);

      verify(categoryRepository).findById(categoryToBeUpdated.getId());
      verify(categoryRepository).save(sameCategoryUpdated);
    }

  }
}

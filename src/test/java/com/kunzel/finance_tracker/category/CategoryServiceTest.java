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

  private void givenNameAvailable(String name) {
    when(categoryRepository.findExistingCategoryByName(name)).thenReturn(Optional.empty());
  }

  private void givenNameTakenBy(Category categoryOwner) {
    when(categoryRepository.findExistingCategoryByName(categoryOwner.getName()))
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
    void shouldThrowExceptionWhenCreatingCategoryWithDuplicateName() {
      // ARRANGE — repo diz que já existe categoria com esse nome+tipo.
      // Fixture com id → simula categoria já persistida (assertNameAvailable lê
      // getId()).
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação");

      givenNameTakenBy(existingCategory);

      // ACT + ASSERT — service deve barrar a duplicata.
      assertThatThrownBy(() -> categoryService.createCategory("Alimentação"))
          .isInstanceOf(IllegalArgumentException.class);

      // Verifica a INTERAÇÃO: como lançou, nunca deve ter tentado salvar.
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldCreateCustomCategoryWhenNameAreAvailable() {
      givenNameAvailable("Natação");

      givenCategorySaveReturnsArgument();

      Category category = categoryService.createCategory("Natação");

      assertThat(category.getName()).isEqualTo("Natação");
      assertThat(category.isDefault()).isFalse();

      verify(categoryRepository).save(category);
    }

    @Test
    void shouldCreateDefaultCategoryWhenNameAreAvailable() {
      givenNameAvailable("Salário");

      givenCategorySaveReturnsArgument();

      Category category = categoryService.createDefaultCategory("Salário");

      assertThat(category.getName()).isEqualTo("Salário");
      assertThat(category.isDefault()).isTrue();

      verify(categoryRepository).save(category);
    }

  }

  @Nested
  class Delete {
    @Test
    void shouldRemoveExistingCategory() {
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação");

      givenCategoryExists(existingCategory);
      givenCategoryHasNoTransactions(existingCategory.getId());

      categoryService.removeCategory(existingCategory.getId());

      verify(categoryRepository).findById(existingCategory.getId());
      verify(categoryRepository).delete(existingCategory);
    }

    @Test
    void shouldThrowExceptionWhenRemovingCategoryWithTransactions() {
      Category categoryWithTransactions = CategoryTestFixtures.withId(1L, "Alimentação");

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
      Category defaultCategory = CategoryTestFixtures.defaultWithId(1L, "Freelance");

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
      Category category1 = CategoryTestFixtures.withId(1L, "Alimentação");
      Category category2 = CategoryTestFixtures.withId(2L, "Salário");

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
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação");

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
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação");
      String newCategoryName = "Alimentação Shopping";

      givenCategoryExists(existingCategory);
      givenCategorySaveReturnsArgument();

      Category updatedCategory = categoryService.updateCategory(existingCategory.getId(), newCategoryName);

      assertThat(updatedCategory.getName()).isEqualTo(newCategoryName);
      assertThat(updatedCategory.getId()).isEqualByComparingTo(existingCategory.getId());

      verify(categoryRepository).findById(updatedCategory.getId());
      verify(categoryRepository).save(updatedCategory);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingDefaultCategory() {
      Category existingDefaultCategory = CategoryTestFixtures.defaultWithId(1L, "Categoria Padrão");

      givenCategoryExists(existingDefaultCategory);

      assertThatThrownBy(
          () -> categoryService.updateCategory(existingDefaultCategory.getId(), "Nova Categoria Padrão"))
          .isInstanceOf(IllegalArgumentException.class);

      verify(categoryRepository).findById(existingDefaultCategory.getId());
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCustomCategoryWhenNameBelongToItself() {
      Category categoryToBeUpdated = CategoryTestFixtures.withId(1L, "Freelance");

      givenCategoryExists(categoryToBeUpdated);
      givenNameTakenBy(categoryToBeUpdated);

      givenCategorySaveReturnsArgument();

      Category sameCategoryUpdated = categoryService.updateCategory(categoryToBeUpdated.getId(), "Freelance");

      assertThat(sameCategoryUpdated).isEqualTo(categoryToBeUpdated);

      verify(categoryRepository).findById(categoryToBeUpdated.getId());
      verify(categoryRepository).save(sameCategoryUpdated);
    }

  }
}

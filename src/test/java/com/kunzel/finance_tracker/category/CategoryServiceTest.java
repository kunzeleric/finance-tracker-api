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

import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;
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

  private void givenCategoryMissing(Long categoryId) {
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
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
      assertThatThrownBy(() -> categoryService.createCategory("Alimentação", CategoryType.EXPENSE, null))
          .isInstanceOf(BusinessRuleException.class);

      // Verifica a INTERAÇÃO: como lançou, nunca deve ter tentado salvar.
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldCreateCustomCategoryWhenNameIsAvailable() {
      givenNameAvailable("Natação");

      givenCategorySaveReturnsArgument();

      Category category = categoryService.createCategory("Natação", CategoryType.EXPENSE, "#3B82F6");

      assertThat(category.getName()).isEqualTo("Natação");
      assertThat(category.isDefault()).isFalse();
      assertThat(category.getType()).isEqualTo(CategoryType.EXPENSE);
      assertThat(category.getColor()).isEqualTo("#3B82F6");

      verify(categoryRepository).save(category);
    }

    @Test
    void shouldCreateDefaultCategoryWhenNameIsAvailable() {
      givenNameAvailable("Salário");

      givenCategorySaveReturnsArgument();

      Category category = categoryService.createDefaultCategory("Salário", CategoryType.INCOME, null);

      assertThat(category.getName()).isEqualTo("Salário");
      assertThat(category.isDefault()).isTrue();
      assertThat(category.getType()).isEqualTo(CategoryType.INCOME);

      verify(categoryRepository).save(category);
    }

  }

  @Nested
  class Delete {
    @Test
    void shouldCascadeDeleteTransactionsWhenNoReassignTargetIsGiven() {
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação");

      givenCategoryExists(existingCategory);

      categoryService.removeCategory(existingCategory.getId(), null);

      verify(transactionRepository).deleteByCategoryId(existingCategory.getId());
      verify(categoryRepository).delete(existingCategory);
    }

    @Test
    void shouldReassignTransactionsWhenTargetIsGiven() {
      Category categoryToRemove = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);
      Category reassignTarget = CategoryTestFixtures.withId(2L, "Mercado", CategoryType.EXPENSE);

      givenCategoryExists(categoryToRemove);
      givenCategoryExists(reassignTarget);

      categoryService.removeCategory(categoryToRemove.getId(), reassignTarget.getId());

      verify(transactionRepository).reassignCategory(categoryToRemove.getId(), reassignTarget);
      verify(transactionRepository, never()).deleteByCategoryId(anyLong());
      verify(categoryRepository).delete(categoryToRemove);
    }

    @Test
    void shouldThrowExceptionWhenReassignTargetDoesNotExist() {
      Category categoryToRemove = CategoryTestFixtures.withId(1L, "Alimentação");
      Long inexistentTargetId = 99L;

      givenCategoryExists(categoryToRemove);
      givenCategoryMissing(inexistentTargetId);

      assertThatThrownBy(() -> categoryService.removeCategory(categoryToRemove.getId(), inexistentTargetId))
          .isInstanceOf(NotFoundException.class);

      verify(categoryRepository, never()).delete(any());
      verify(transactionRepository, never()).deleteByCategoryId(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenReassignTargetHasDifferentType() {
      Category expenseCategory = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);
      Category incomeCategory = CategoryTestFixtures.withId(2L, "Salário", CategoryType.INCOME);

      givenCategoryExists(expenseCategory);
      givenCategoryExists(incomeCategory);

      assertThatThrownBy(() -> categoryService.removeCategory(expenseCategory.getId(), incomeCategory.getId()))
          .isInstanceOf(BusinessRuleException.class);

      verify(categoryRepository, never()).delete(any());
      verify(transactionRepository, never()).reassignCategory(anyLong(), any());
    }

    @Test
    void shouldThrowExceptionWhenReassignTargetIsTheCategoryItself() {
      Category categoryToRemove = CategoryTestFixtures.withId(1L, "Alimentação");

      givenCategoryExists(categoryToRemove);

      assertThatThrownBy(() -> categoryService.removeCategory(categoryToRemove.getId(), categoryToRemove.getId()))
          .isInstanceOf(ValidationException.class);

      verify(categoryRepository, never()).delete(any());
      verify(transactionRepository, never()).deleteByCategoryId(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenDeletingCategoryWithInvalidId() {
      Long inexistentCategoryId = 2L;

      givenCategoryMissing(inexistentCategoryId);

      assertThatThrownBy(() -> categoryService.removeCategory(inexistentCategoryId, null))
          .isInstanceOf(NotFoundException.class);

      verify(categoryRepository, never()).delete(any());
      verify(transactionRepository, never()).deleteByCategoryId(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenDeletingDefaultCategory() {
      Category defaultCategory = CategoryTestFixtures.defaultWithId(1L, "Freelance");

      givenCategoryExists(defaultCategory);

      assertThatThrownBy(() -> categoryService.removeCategory(defaultCategory.getId(), null))
          .isInstanceOf(BusinessRuleException.class);

      verify(categoryRepository).findById(defaultCategory.getId());
      verify(categoryRepository, never()).delete(any());
      verify(transactionRepository, never()).deleteByCategoryId(anyLong());
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

      Category updatedCategory = categoryService.updateCategory(existingCategory.getId(), newCategoryName, null, null);

      assertThat(updatedCategory.getName()).isEqualTo(newCategoryName);
      assertThat(updatedCategory.getId()).isEqualByComparingTo(existingCategory.getId());

      verify(categoryRepository).findById(updatedCategory.getId());
      verify(categoryRepository).save(updatedCategory);
    }

    @Test
    void shouldUpdateCustomCategoryType() {
      Category existingCategory = CategoryTestFixtures.withId(1L, "Freelance", CategoryType.EXPENSE);

      givenCategoryExists(existingCategory);
      givenCategorySaveReturnsArgument();

      Category updatedCategory = categoryService.updateCategory(existingCategory.getId(), null, null,
          CategoryType.INCOME);

      assertThat(updatedCategory.getType()).isEqualTo(CategoryType.INCOME);

      verify(categoryRepository).save(updatedCategory);
    }

    @Test
    void shouldUpdateColorOfDefaultCategory() {
      Category existingDefaultCategory = CategoryTestFixtures.defaultWithId(1L, "Categoria Padrão");

      givenCategoryExists(existingDefaultCategory);
      givenCategorySaveReturnsArgument();

      Category updatedCategory = categoryService.updateCategory(existingDefaultCategory.getId(), null, "#EC4899", null);

      assertThat(updatedCategory.getColor()).isEqualTo("#EC4899");

      verify(categoryRepository).save(updatedCategory);
    }

    @Test
    void shouldThrowExceptionWhenRenamingDefaultCategory() {
      Category existingDefaultCategory = CategoryTestFixtures.defaultWithId(1L, "Categoria Padrão");

      givenCategoryExists(existingDefaultCategory);

      assertThatThrownBy(
          () -> categoryService.updateCategory(existingDefaultCategory.getId(), "Nova Categoria Padrão", null, null))
          .isInstanceOf(BusinessRuleException.class);

      verify(categoryRepository).findById(existingDefaultCategory.getId());
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenChangingTypeOfDefaultCategory() {
      Category existingDefaultCategory = CategoryTestFixtures.defaultWithId(1L, "Categoria Padrão",
          CategoryType.EXPENSE);

      givenCategoryExists(existingDefaultCategory);

      assertThatThrownBy(
          () -> categoryService.updateCategory(existingDefaultCategory.getId(), null, null, CategoryType.INCOME))
          .isInstanceOf(BusinessRuleException.class);

      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCustomCategoryWhenNameBelongToItself() {
      Category categoryToBeUpdated = CategoryTestFixtures.withId(1L, "Freelance");

      givenCategoryExists(categoryToBeUpdated);
      givenNameTakenBy(categoryToBeUpdated);

      givenCategorySaveReturnsArgument();

      Category sameCategoryUpdated = categoryService.updateCategory(categoryToBeUpdated.getId(), "Freelance", null,
          null);

      assertThat(sameCategoryUpdated).isEqualTo(categoryToBeUpdated);

      verify(categoryRepository).findById(categoryToBeUpdated.getId());
      verify(categoryRepository).save(sameCategoryUpdated);
    }

  }
}

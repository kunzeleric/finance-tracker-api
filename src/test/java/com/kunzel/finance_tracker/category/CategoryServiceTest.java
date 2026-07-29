package com.kunzel.finance_tracker.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryService categoryService;

  @Nested
  class Create {
    @Test
    void shouldThrowExceptionWhenCreatingCategoryWithDuplicateNameAndType() {
      // ARRANGE — repo diz que já existe categoria com esse nome+tipo.
      // Fixture com id → simula categoria já persistida (assertNameTypeAvailable lê
      // getId()).
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);

      when(categoryRepository.findExistingCategoryByNameAndType("Alimentação", CategoryType.EXPENSE))
          .thenReturn(Optional.of(existingCategory));

      // ACT + ASSERT — service deve barrar a duplicata.
      assertThatThrownBy(() -> categoryService.createCategory("Alimentação", CategoryType.EXPENSE))
          .isInstanceOf(IllegalArgumentException.class);

      // Verifica a INTERAÇÃO: como lançou, nunca deve ter tentado salvar.
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldCreateCustomCategoryWhenNameAndTypeAreAvailable() {
      when(categoryRepository.findExistingCategoryByNameAndType("Natação", CategoryType.EXPENSE))
          .thenReturn(Optional.empty());

      when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

      Category category = categoryService.createCategory("Natação", CategoryType.EXPENSE);

      assertThat(category.getName()).isEqualTo("Natação");
      assertThat(category.getType()).isEqualTo(CategoryType.EXPENSE);
      assertThat(category.isDefault()).isFalse();

      verify(categoryRepository).save(category);
    }

    @Test
    void shouldCreateDefaultCategoryWhenNameAndTypeAreAvailable() {
      when(categoryRepository.findExistingCategoryByNameAndType("Salário", CategoryType.INCOME))
          .thenReturn(Optional.empty());

      when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

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

      // confirma que categoria existe
      when(categoryRepository.findById(existingCategory.getId()))
          .thenReturn(Optional.of(existingCategory));

      categoryService.removeCategory(existingCategory.getId());

      verify(categoryRepository).findById(existingCategory.getId());
      verify(categoryRepository).delete(existingCategory);
    }

    @Test
    void shouldThrowExceptionWhenDeletingCategoryWithInvalidId() {
      Long inexistentCategoryId = 2L;

      // confirma que categoria não existe
      when(categoryRepository.findById(inexistentCategoryId))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> categoryService.removeCategory(inexistentCategoryId))
          .isInstanceOf(NotFoundException.class);

      verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingDefaultCategory() {
      Category defaultCategory = CategoryTestFixtures.defaultIncomeCategory();

      when(categoryRepository.findById(defaultCategory.getId())).thenReturn(Optional.of(defaultCategory));

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

      when(categoryRepository.findById(existingCategory.getId())).thenReturn(Optional.of(existingCategory));

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
    void shouldUpdateCategoryWithValidName() {
      Category existingCategory = CategoryTestFixtures.withId(1L, "Alimentação", CategoryType.EXPENSE);
      String newCategoryName = "Alimentação Shopping";

      // espera que categoria exista
      when(categoryRepository.findById(existingCategory.getId())).thenReturn(Optional.of(existingCategory));
      // espera que não tenha conflito de nomes
      when(categoryRepository.findExistingCategoryByNameAndType(newCategoryName, existingCategory.getType()))
          .thenReturn(Optional.empty());
      // deixa explicito o retorno do mock
      when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

      Category updatedCategory = categoryService.updateCategory(existingCategory.getId(), newCategoryName,
          CategoryType.EXPENSE);

      assertThat(updatedCategory.getName()).isEqualTo(newCategoryName);
      assertThat(updatedCategory.getId()).isEqualByComparingTo(existingCategory.getId());

      verify(categoryRepository).findById(updatedCategory.getId());
      verify(categoryRepository).save(updatedCategory);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingCategoryWithExistingNameAndType() {
      Category regularCategory = CategoryTestFixtures.withId(1L, "Freelance", CategoryType.INCOME);
      Category categoryToBeUpdated = CategoryTestFixtures.withId(2L, "Salário", CategoryType.INCOME);

      when(categoryRepository.findById(categoryToBeUpdated.getId())).thenReturn(Optional.of(categoryToBeUpdated));
      when(categoryRepository.findExistingCategoryByNameAndType("Freelance", CategoryType.INCOME))
          .thenReturn(Optional.of(regularCategory));

      assertThatThrownBy(
          () -> categoryService.updateCategory(categoryToBeUpdated.getId(), "Freelance", CategoryType.INCOME))
          .isInstanceOf(IllegalArgumentException.class);

      verify(categoryRepository).findById(categoryToBeUpdated.getId());
      verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCategoryWhenNameAndTypeBelongToItself() {
      Category categoryToBeUpdated = CategoryTestFixtures.withId(1L, "Freelance", CategoryType.INCOME);

      when(categoryRepository.findById(categoryToBeUpdated.getId())).thenReturn(Optional.of(categoryToBeUpdated));
      when(categoryRepository.findExistingCategoryByNameAndType("Freelance", CategoryType.INCOME))
          .thenReturn(Optional.of(categoryToBeUpdated));

      when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

      Category sameCategoryUpdated = categoryService.updateCategory(categoryToBeUpdated.getId(), "Freelance",
          CategoryType.INCOME);

      assertThat(sameCategoryUpdated).isEqualTo(categoryToBeUpdated);

      verify(categoryRepository).findById(categoryToBeUpdated.getId());
      verify(categoryRepository).save(sameCategoryUpdated);
    }

  }
}

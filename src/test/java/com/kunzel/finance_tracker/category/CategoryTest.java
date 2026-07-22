package com.kunzel.finance_tracker.category;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CategoryTest {
  Category category;

  @BeforeEach
  void setup() {
    category = new Category("Alimentação", CategoryType.EXPENSE, true);
  }

  @Nested
  class Constructor {

    @Test
    void shouldCreateDefaultCategory() {
      assertThat(category.getName()).isEqualTo("Alimentação");
      assertThat(category.getType()).isEqualTo(CategoryType.EXPENSE);
      assertThat(category.isDefault()).isTrue();
    }

    @Test
    void shouldCreateCustomCategory() {
      Category customCategory = new Category("Natação", CategoryType.EXPENSE);
      assertThat(customCategory.isDefault()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenCustomCategoryNameIsEmpty() {
      assertThatThrownBy(() -> new Category("", CategoryType.EXPENSE)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenCustomCategoryTypeIsEmpty() {
      assertThatThrownBy(() -> new Category("Natação", null)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class Rename {

    @Test
    void shouldRenameCustomCategory() {
      Category customCategory = new Category("Freelance", CategoryType.INCOME);
      String newName = "Projetos";

      customCategory.rename(newName);
      assertThat(customCategory.getName()).isEqualTo(newName);
    }

    @Test
    void shouldThrowExceptionWhenRenamingDefaultCategory() {
      String newName = "Freelance";
      assertThatThrownBy(() -> category.rename(newName)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowExceptionWhenRenamingCustomCategoryWithEmptyName() {
      Category customCategory = new Category("Freelance", CategoryType.INCOME);
      assertThatThrownBy(() -> customCategory.rename(null)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class Type {

    @Test
    void shouldChangeCustomCategoryType() {
      Category customCategory = new Category("Natação", CategoryType.INCOME);
      customCategory.changeType(CategoryType.EXPENSE);
      assertThat(customCategory.getType()).isEqualTo(CategoryType.EXPENSE);
    }

    @Test
    void shouldThrowExceptionWhenChangingDefaultCategoryType() {
      assertThatThrownBy(() -> category.changeType(CategoryType.INCOME)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowExceptionWhenChangingCustomCategoryTypeToEmpty() {
      Category customCategory = new Category("Natação", CategoryType.INCOME);
      assertThatThrownBy(() -> customCategory.changeType(null)).isInstanceOf(IllegalArgumentException.class);
    }
  }
}

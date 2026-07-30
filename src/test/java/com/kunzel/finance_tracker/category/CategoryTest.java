package com.kunzel.finance_tracker.category;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CategoryTest {
  Category category;

  @BeforeEach
  void setup() {
    category = Category.createDefault("Alimentação");
  }

  @Nested
  class Constructor {

    @Test
    void shouldCreateDefaultCategory() {
      assertThat(category.getName()).isEqualTo("Alimentação");
      assertThat(category.isDefault()).isTrue();
    }

    @Test
    void shouldCreateCustomCategory() {
      Category customCategory = Category.createCustom("Natação");
      assertThat(customCategory.isDefault()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenCustomCategoryNameIsEmpty() {
      assertThatThrownBy(() -> Category.createCustom(""))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenCustomCategoryNameIsBlank() {
      assertThatThrownBy(() -> Category.createCustom("  "))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenCustomCategoryNameIsNull() {
      assertThatThrownBy(() -> Category.createCustom(null))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class Rename {

    @Test
    void shouldRenameCustomCategory() {
      Category customCategory = Category.createCustom("Freelance");
      String newName = "Projetos";

      customCategory.update(newName);
      assertThat(customCategory.getName()).isEqualTo(newName);
    }

    @Test
    void shouldThrowExceptionWhenRenamingDefaultCategory() {
      String newName = "Freelance";
      assertThatThrownBy(() -> category.update(newName)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowExceptionWhenRenamingCustomCategoryWithNullName() {
      Category customCategory = Category.createCustom("Freelance");
      assertThatThrownBy(() -> customCategory.update(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenRenamingCustomCategoryWithEmptyName() {
      Category customCategory = Category.createCustom("Freelance");
      assertThatThrownBy(() -> customCategory.update("")).isInstanceOf(IllegalArgumentException.class);
    }
  }
}

package com.kunzel.finance_tracker.category;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kunzel.finance_tracker.shared.HexColor;
import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

public class CategoryTest {
  Category category;

  @BeforeEach
  void setup() {
    category = Category.createDefault("Alimentação", CategoryType.EXPENSE, "#F97316");
  }

  @Nested
  class Constructor {

    @Test
    void shouldCreateDefaultCategory() {
      assertThat(category.getName()).isEqualTo("Alimentação");
      assertThat(category.isDefault()).isTrue();
      assertThat(category.getType()).isEqualTo(CategoryType.EXPENSE);
      assertThat(category.getColor()).isEqualTo("#F97316");
    }

    @Test
    void shouldCreateCustomCategory() {
      Category customCategory = Category.createCustom("Natação", CategoryType.EXPENSE, "#3B82F6");
      assertThat(customCategory.isDefault()).isFalse();
      assertThat(customCategory.getType()).isEqualTo(CategoryType.EXPENSE);
    }

    @Test
    void shouldThrowExceptionWhenCustomCategoryNameIsEmpty() {
      assertThatThrownBy(() -> Category.createCustom("", CategoryType.EXPENSE, null))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenCustomCategoryNameIsBlank() {
      assertThatThrownBy(() -> Category.createCustom("  ", CategoryType.EXPENSE, null))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenCustomCategoryNameIsNull() {
      assertThatThrownBy(() -> Category.createCustom(null, CategoryType.EXPENSE, null))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
      assertThatThrownBy(() -> Category.createCustom("Natação", null, null))
          .isInstanceOf(ValidationException.class);
    }
  }

  @Nested
  class Color {

    @Test
    void shouldFallBackToDefaultColorWhenColorIsNull() {
      Category customCategory = Category.createCustom("Natação", CategoryType.EXPENSE, null);
      assertThat(customCategory.getColor()).isEqualTo(HexColor.DEFAULT);
    }

    @Test
    void shouldFallBackToDefaultColorWhenColorIsBlank() {
      Category customCategory = Category.createCustom("Natação", CategoryType.EXPENSE, "  ");
      assertThat(customCategory.getColor()).isEqualTo(HexColor.DEFAULT);
    }

    @Test
    void shouldNormalizeColorToUpperCase() {
      Category customCategory = Category.createCustom("Natação", CategoryType.EXPENSE, "#a1b2c3");
      assertThat(customCategory.getColor()).isEqualTo("#A1B2C3");
    }

    @Test
    void shouldThrowExceptionWhenColorIsNotAValidHex() {
      assertThatThrownBy(() -> Category.createCustom("Natação", CategoryType.EXPENSE, "vermelho"))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenColorIsMissingHash() {
      assertThatThrownBy(() -> Category.createCustom("Natação", CategoryType.EXPENSE, "A1B2C3"))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenColorIsShorthandHex() {
      assertThatThrownBy(() -> Category.createCustom("Natação", CategoryType.EXPENSE, "#FFF"))
          .isInstanceOf(ValidationException.class);
    }
  }

  @Nested
  class Update {

    @Test
    void shouldUpdateCustomCategoryName() {
      Category customCategory = Category.createCustom("Freelance", CategoryType.INCOME, null);

      customCategory.update("Projetos", null, null);

      assertThat(customCategory.getName()).isEqualTo("Projetos");
    }

    @Test
    void shouldUpdateCustomCategoryType() {
      Category customCategory = Category.createCustom("Freelance", CategoryType.INCOME, null);

      customCategory.update(null, null, CategoryType.EXPENSE);

      assertThat(customCategory.getType()).isEqualTo(CategoryType.EXPENSE);
    }

    @Test
    void shouldUpdateCustomCategoryColor() {
      Category customCategory = Category.createCustom("Freelance", CategoryType.INCOME, null);

      customCategory.update(null, "#14B8A6", null);

      assertThat(customCategory.getColor()).isEqualTo("#14B8A6");
    }

    @Test
    void shouldKeepCurrentColorWhenColorIsNull() {
      Category customCategory = Category.createCustom("Freelance", CategoryType.INCOME, "#14B8A6");

      customCategory.update("Projetos", null, null);

      assertThat(customCategory.getColor()).isEqualTo("#14B8A6");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingCustomCategoryWithBlankName() {
      Category customCategory = Category.createCustom("Freelance", CategoryType.INCOME, null);

      assertThatThrownBy(() -> customCategory.update("", null, null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldUpdateDefaultCategoryColor() {
      category.update(null, "#EC4899", null);

      assertThat(category.getColor()).isEqualTo("#EC4899");
    }

    @Test
    void shouldUpdateDefaultCategoryColorWhenNameAndTypeAreUnchanged() {
      category.update("Alimentação", "#EC4899", CategoryType.EXPENSE);

      assertThat(category.getColor()).isEqualTo("#EC4899");
      assertThat(category.getName()).isEqualTo("Alimentação");
    }

    @Test
    void shouldThrowExceptionWhenRenamingDefaultCategory() {
      assertThatThrownBy(() -> category.update("Freelance", null, null))
          .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldThrowExceptionWhenChangingTypeOfDefaultCategory() {
      assertThatThrownBy(() -> category.update(null, null, CategoryType.INCOME))
          .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldNotMutateDefaultCategoryColorWhenNameChangeIsRejected() {
      assertThatThrownBy(() -> category.update("Freelance", "#EC4899", null))
          .isInstanceOf(BusinessRuleException.class);

      assertThat(category.getColor()).isEqualTo("#F97316");
    }
  }
}

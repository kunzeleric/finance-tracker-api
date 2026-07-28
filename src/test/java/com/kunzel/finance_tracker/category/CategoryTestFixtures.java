package com.kunzel.finance_tracker.category;

import org.springframework.test.util.ReflectionTestUtils;

class CategoryTestFixtures {
  static Category customExpenseCategory() {
    return Category.createCustom("Alimentação", CategoryType.EXPENSE);
  }

  static Category customIncomeCategory() {
    return Category.createCustom("Salário", CategoryType.INCOME);
  }

  static Category defaultIncomeCategory() {
    return Category.createDefault("Freelance", CategoryType.INCOME);
  }

  static Category defaultExpenseCategory() {
    return Category.createDefault("Aluguel", CategoryType.EXPENSE);
  }

  static Category withId(Long id, String name, CategoryType type) {
    Category category = Category.createCustom(name, type);
    ReflectionTestUtils.setField(category, "id", id);
    return category;
  }
}

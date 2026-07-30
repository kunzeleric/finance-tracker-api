package com.kunzel.finance_tracker.category;

import org.springframework.test.util.ReflectionTestUtils;

public class CategoryTestFixtures {
  public static Category customExpenseCategory() {
    return Category.createCustom("Alimentação", CategoryType.EXPENSE);
  }

  public static Category customIncomeCategory() {
    return Category.createCustom("Salário", CategoryType.INCOME);
  }

  public static Category defaultIncomeCategory() {
    return Category.createDefault("Freelance", CategoryType.INCOME);
  }

  public static Category defaultExpenseCategory() {
    return Category.createDefault("Aluguel", CategoryType.EXPENSE);
  }

  public static Category incomeWithId(Long id) {
    return withId(id, "Categoria Income Teste", CategoryType.INCOME);
  }

  public static Category expenseWithId(Long id) {
    return withId(id, "Categoria Expense Teste", CategoryType.EXPENSE);
  }

  public static Category withId(Long id, String name, CategoryType type) {
    Category category = Category.createCustom(name, type);
    ReflectionTestUtils.setField(category, "id", id);
    return category;
  }

  public static Category defaultWithId(Long id, String name, CategoryType type) {
    Category category = Category.createDefault(name, type);
    ReflectionTestUtils.setField(category, "id", id);
    return category;
  }
}

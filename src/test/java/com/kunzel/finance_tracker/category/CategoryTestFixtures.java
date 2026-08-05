package com.kunzel.finance_tracker.category;

import org.springframework.test.util.ReflectionTestUtils;

public class CategoryTestFixtures {
  public static Category customCategory() {
    return Category.createCustom("Categoria Customizada", CategoryType.EXPENSE, null);
  }

  public static Category defaultCategory() {
    return Category.createDefault("Categoria Padrão", CategoryType.EXPENSE, null);
  }

  public static Category withId(Long id, String name) {
    return withId(id, name, CategoryType.EXPENSE);
  }

  public static Category withId(Long id, String name, CategoryType type) {
    Category category = Category.createCustom(name, type, null);
    ReflectionTestUtils.setField(category, "id", id);
    return category;
  }

  public static Category defaultWithId(Long id, String name) {
    return defaultWithId(id, name, CategoryType.EXPENSE);
  }

  public static Category defaultWithId(Long id, String name, CategoryType type) {
    Category category = Category.createDefault(name, type, null);
    ReflectionTestUtils.setField(category, "id", id);
    return category;
  }
}

package com.kunzel.finance_tracker.category;

import org.springframework.test.util.ReflectionTestUtils;

public class CategoryTestFixtures {
  public static Category customCategory() {
    return Category.createCustom("Categoria Customizada");
  }

  public static Category defaultCategory() {
    return Category.createDefault("Categoria Padrão");
  }

  public static Category withId(Long id, String name) {
    Category category = Category.createCustom(name);
    ReflectionTestUtils.setField(category, "id", id);
    return category;
  }

  public static Category defaultWithId(Long id, String name) {
    Category category = Category.createDefault(name);
    ReflectionTestUtils.setField(category, "id", id);
    return category;
  }
}

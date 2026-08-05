package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryType;

public record CategoryResponse(Long categoryId, String name, CategoryType type, String color,
    Boolean isDefault) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.getColor(),
        category.isDefault());
  }
}

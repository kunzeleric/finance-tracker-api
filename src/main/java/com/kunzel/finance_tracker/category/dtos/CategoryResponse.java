package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryType;

public record CategoryResponse(Long categoryId, String name,
    CategoryType type,
    Boolean isDefault) {

  public CategoryResponse(Category category) {
    this(category.getId(), category.getName(), category.getType(), category.isDefault());
  }
}

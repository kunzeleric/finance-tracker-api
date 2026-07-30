package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.Category;

public record CategoryResponse(Long categoryId, String name,
    String type,
    Boolean isDefault) {

  public CategoryResponse(Category category) {
    this(category.getId(), category.getName(), category.getType().getDescription(), category.isDefault());
  }
}

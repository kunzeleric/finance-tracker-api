package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.Category;

public record CategoryResponse(Long categoryId, String name,
    Boolean isDefault) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(category.getId(), category.getName(), category.isDefault());
  }
}

package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.Category;

public record CategorySummary(Long categoryId, String name, Boolean isDefault, String type) {
  public static CategorySummary from(Category category) {
    return new CategorySummary(category.getId(), category.getName(), category.isDefault(),
        category.getType().getDescription());
  }
}

package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryResponse(@NotBlank(message = "nao pode estar em branco") String name,
    @NotNull(message = "não pode estar em branco") CategoryType type,
    @NotNull(message = "não pode estar em branco") Boolean isDefault) {

  public CategoryResponse(Category category) {
    this(category.getName(), category.getType(), category.isDefault());
  }
}

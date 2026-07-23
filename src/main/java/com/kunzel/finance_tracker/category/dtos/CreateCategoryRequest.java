package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.CategoryType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(@NotBlank(message = "não pode estar em branco") String name,
    @NotNull(message = "não pode estar em branco") CategoryType type) {

}

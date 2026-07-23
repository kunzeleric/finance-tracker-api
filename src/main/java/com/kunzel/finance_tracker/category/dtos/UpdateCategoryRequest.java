package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.CategoryType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateCategoryRequest(@Pattern(regexp = ".*\\S.*", message = "não pode estar em branco") String name,
    @NotNull(message = "não pode estar em branco") CategoryType type) {

}

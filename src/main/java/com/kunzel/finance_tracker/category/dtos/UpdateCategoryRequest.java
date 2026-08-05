package com.kunzel.finance_tracker.category.dtos;

import com.kunzel.finance_tracker.category.CategoryType;
import com.kunzel.finance_tracker.shared.HexColor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCategoryRequest(
    @NotBlank(message = "não pode estar em branco") String name,
    CategoryType type,
    @Pattern(regexp = HexColor.PATTERN, message = "deve estar no formato #RRGGBB") String color) {

}

package com.kunzel.finance_tracker.category.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(
        @NotNull(message = "não pode estar em branco") @NotBlank(message = "não pode estar em branco") String name) {

}

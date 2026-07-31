package com.kunzel.finance_tracker.category.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "não pode estar em branco") String name) {

}

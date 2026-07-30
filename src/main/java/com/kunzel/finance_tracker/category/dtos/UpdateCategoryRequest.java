package com.kunzel.finance_tracker.category.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateCategoryRequest(
                @NotNull(message = "não pode estar em branco") @Pattern(regexp = ".*\\S.*", message = "não pode estar em branco") String name) {

}

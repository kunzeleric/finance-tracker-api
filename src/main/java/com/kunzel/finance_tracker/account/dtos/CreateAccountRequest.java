package com.kunzel.finance_tracker.account.dtos;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.account.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequest(
        @NotBlank(message = "não deve estar em branco") String name,
        @NotNull(message = "não pode ser nulo") AccountType type,
        @PositiveOrZero(message = "não pode ser negativo") BigDecimal balance) {
}

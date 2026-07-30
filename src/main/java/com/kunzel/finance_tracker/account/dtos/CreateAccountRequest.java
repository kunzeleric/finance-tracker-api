package com.kunzel.finance_tracker.account.dtos;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.account.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequest(
        @NotBlank(message = "não pode estar em branco") String name,
        @NotNull(message = "não pode estar em branco") AccountType type,
        @NotNull(message = "não pode estar em branco") @PositiveOrZero(message = "não pode ser negativo") BigDecimal initialBalance) {
}

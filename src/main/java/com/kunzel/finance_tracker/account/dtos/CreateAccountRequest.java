package com.kunzel.finance_tracker.account.dtos;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.account.AccountType;
import com.kunzel.finance_tracker.shared.HexColor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequest(
        @NotBlank(message = "não pode estar em branco") String name,
        @NotNull(message = "é obrigatório") AccountType type,
        @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") BigDecimal openingBalance,
        @Pattern(regexp = HexColor.PATTERN, message = "deve estar no formato #RRGGBB") String color,
        String institution) {
}

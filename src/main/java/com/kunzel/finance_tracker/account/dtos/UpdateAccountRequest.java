
package com.kunzel.finance_tracker.account.dtos;

import com.kunzel.finance_tracker.account.AccountType;
import com.kunzel.finance_tracker.shared.HexColor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateAccountRequest(
    @NotBlank(message = "não pode estar em branco") String name,
    @NotNull(message = "é obrigatório") AccountType type,
    @Pattern(regexp = HexColor.PATTERN, message = "deve estar no formato #RRGGBB") String color,
    String institution) {
}

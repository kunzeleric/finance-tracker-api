
package com.kunzel.finance_tracker.account.dtos;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.account.AccountType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAccountRequest(
    @Pattern(regexp = ".*\\S.*", message = "não deve estar em branco") String name,
    @NotNull(message = "não pode ser nulo") AccountType type,
    @PositiveOrZero(message = "não pode ser negativo") BigDecimal balance) {
}

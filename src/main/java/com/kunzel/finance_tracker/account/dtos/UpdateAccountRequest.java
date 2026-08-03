
package com.kunzel.finance_tracker.account.dtos;

import com.kunzel.finance_tracker.account.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountRequest(
                @NotBlank(message = "não pode estar em branco") String name,
                @NotNull(message = "é obrigatório") AccountType type) {
}

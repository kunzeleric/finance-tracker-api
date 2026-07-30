
package com.kunzel.finance_tracker.account.dtos;

import com.kunzel.finance_tracker.account.AccountType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateAccountRequest(
                @NotNull(message = "não pode estar em branco") @Pattern(regexp = ".*\\S.*", message = "não pode estar em branco") String name,
                @NotNull(message = "não pode estar em branco") AccountType type) {
}

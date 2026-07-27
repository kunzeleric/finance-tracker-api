
package com.kunzel.finance_tracker.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTransactionRequest(@NotBlank(message = "não pode estar em branco") String description,
    @NotNull(message = "não pode estar em branco") @Positive(message = "não pode ser igual ou menor que 0 (zero)") BigDecimal amount,
    @NotNull(message = "não pode estar em branco") LocalDate date,
    @NotNull(message = "não pode estar em branco") Long accountId,
    @NotNull(message = "não pode estar em branco") Long categoryId) {

}

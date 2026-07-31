
package com.kunzel.finance_tracker.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kunzel.finance_tracker.transaction.TransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTransactionRequest(@NotBlank(message = "não pode estar em branco") String description,
        @NotNull(message = "é obrigatório") TransactionType type,
        @NotNull(message = "é obrigatório") @Positive(message = "não pode ser igual ou menor que 0 (zero)") BigDecimal amount,
        @NotNull(message = "é obrigatório") LocalDate date,
        @NotNull(message = "é obrigatório") Long accountId,
        @NotNull(message = "é obrigatório") Long categoryId) {

}

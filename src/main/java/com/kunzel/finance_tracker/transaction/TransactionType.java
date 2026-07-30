package com.kunzel.finance_tracker.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionType {
  INCOME("Receita"),
  EXPENSE("Despesa");

  private final String description;

  TransactionType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @JsonCreator
  public static TransactionType fromValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return TransactionType.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "Tipo de transação inválido: '" + value + "'. Valores aceitos: INCOME, EXPENSE");
    }
  }
}

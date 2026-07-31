package com.kunzel.finance_tracker.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

public enum AccountType {
  CHECKING("Conta Corrente"),
  SAVINGS("Poupança"),
  WALLET("Carteira"),
  INVESTMENT("Investimento");

  private final String description;

  AccountType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @JsonCreator
  public static AccountType fromValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return AccountType.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new ValidationException(
          "Tipo de conta inválido: '" + value + "'. Valores aceitos: CHECKING, SAVINGS, WALLET, INVESTMENT");
    }
  }
}

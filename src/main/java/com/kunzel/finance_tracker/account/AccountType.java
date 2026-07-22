package com.kunzel.finance_tracker.account;

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
}

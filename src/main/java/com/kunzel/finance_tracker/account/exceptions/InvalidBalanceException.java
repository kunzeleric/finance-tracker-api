package com.kunzel.finance_tracker.account.exceptions;

import java.math.BigDecimal;

public class InvalidBalanceException extends RuntimeException {
  public InvalidBalanceException(BigDecimal balance) {
    super("Saldo inicial não pode ser 0 (zero) ou vazio. Saldo inserido: " + balance);
  }
}

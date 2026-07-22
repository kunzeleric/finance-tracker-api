package com.kunzel.finance_tracker.account.exceptions;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
  public InsufficientBalanceException(BigDecimal amount) {
    super("Saldo insuficiente para saque no valor de " + amount);
  }
}

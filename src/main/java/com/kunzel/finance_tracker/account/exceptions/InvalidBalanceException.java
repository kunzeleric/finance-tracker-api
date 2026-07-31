package com.kunzel.finance_tracker.account.exceptions;

import java.math.BigDecimal;

import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

public class InvalidBalanceException extends ValidationException {
  public InvalidBalanceException(BigDecimal balance) {
    super("Saldo inicial não pode ser menor que 0 (zero) ou vazio. Saldo inserido: " + balance);
  }
}

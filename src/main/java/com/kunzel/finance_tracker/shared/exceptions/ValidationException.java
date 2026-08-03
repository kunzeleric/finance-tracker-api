package com.kunzel.finance_tracker.shared.exceptions;

public class ValidationException extends DomainException {
  public ValidationException(String message) {
    super(message);
  }
}

package com.kunzel.finance_tracker.shared.exceptions;

public abstract class DomainException extends RuntimeException {
  protected DomainException(String message) {
    super(message);
  }
}

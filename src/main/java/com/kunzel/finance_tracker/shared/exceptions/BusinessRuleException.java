package com.kunzel.finance_tracker.shared.exceptions;

public class BusinessRuleException extends DomainException {
  public BusinessRuleException(String message) {
    super(message);
  }
}

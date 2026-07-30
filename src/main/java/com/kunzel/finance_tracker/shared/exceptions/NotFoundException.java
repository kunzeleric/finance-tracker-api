package com.kunzel.finance_tracker.shared.exceptions;

public class NotFoundException extends RuntimeException {
  public NotFoundException(Long id, String resource) {
    super("Recurso do tipo " + resource + " com ID " + id + " não foi encontrado.");
  }
}

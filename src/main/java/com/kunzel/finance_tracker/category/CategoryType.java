package com.kunzel.finance_tracker.category;

import com.fasterxml.jackson.annotation.JsonCreator;

import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

public enum CategoryType {
  INCOME("Receita"),
  EXPENSE("Despesa");

  private final String description;

  CategoryType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @JsonCreator
  public static CategoryType fromValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return CategoryType.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new ValidationException(
          "Tipo de categoria inválido: '" + value + "'. Valores aceitos: INCOME, EXPENSE");
    }
  }
}

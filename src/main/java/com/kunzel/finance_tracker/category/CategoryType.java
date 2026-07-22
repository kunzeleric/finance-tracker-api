package com.kunzel.finance_tracker.category;

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
}
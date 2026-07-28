package com.kunzel.finance_tracker.category;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kunzel.finance_tracker.transaction.Transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
  @SequenceGenerator(name = "category_seq", sequenceName = "category_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CategoryType type;

  @Column(nullable = false)
  private Boolean isDefault;

  @Column(nullable = false)
  private LocalDate creationDate;

  @OneToMany(mappedBy = "category")
  @JsonManagedReference
  private List<Transaction> transactions;

  protected Category() {
  }

  private Category(String name, CategoryType type, Boolean isDefault) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Nome da categoria nao pode estar em branco");
    }

    if (type == null) {
      throw new IllegalArgumentException("Tipo da categoria nao pode estar em branco");
    }

    this.name = name;
    this.type = type;
    this.isDefault = isDefault;
    this.creationDate = LocalDate.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public CategoryType getType() {
    return type;
  }

  public Boolean isDefault() {
    return isDefault;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }

  public void rename(String newName) {
    if (this.isDefault()) {
      throw new IllegalStateException("Categorias do sistema não podem ter alteração de nome");
    }

    if (newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("Nome de categoria não pode estar em branco");
    }

    this.name = newName;
  }

  public void changeType(CategoryType type) {
    if (this.isDefault()) {
      throw new IllegalStateException("Categorias do sistema não podem ter alteração de tipo");
    }

    if (type == null) {
      throw new IllegalArgumentException("Tipo de categoria não pode estar em branco");
    }

    this.type = type;
  }

  public void updateDetails(String name, CategoryType type) {
    rename(name);
    changeType(type);
  }

  public static Category createDefault(String name, CategoryType type) {
    return new Category(name, type, true);
  }

  public static Category createCustom(String name, CategoryType type) {
    return new Category(name, type, false);
  }

  public Boolean isExpense() {
    return type == CategoryType.EXPENSE;
  }
}

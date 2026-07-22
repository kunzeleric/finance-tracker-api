package com.kunzel.finance_tracker.category;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
  @SequenceGenerator(name = "category_seq", sequenceName = "category_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Nome da categoria não pode estar vazio")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @NotBlank(message = "Tipo da categoria não pode estar vazio")
  private CategoryType type;

  @Column(nullable = false)
  private boolean isDefault;

  @Column(nullable = false)
  private LocalDate creationDate;

  protected Category() {
  }

  public Category(String name, CategoryType type) {
    this(name, type, false);
  }

  public Category(String name, CategoryType type, boolean isDefault) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Nome da categoria nao pode estar vazio");
    }

    if (type == null) {
      throw new IllegalArgumentException("Tipo da categoria nao pode estar vazio");
    }

    this.name = name;
    this.type = type;
    this.isDefault = isDefault;
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

  public boolean isDefault() {
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
      throw new IllegalArgumentException("Nome de categoria não pode estar vazio");
    }

    this.name = newName;
  }

  public void changeType(CategoryType type) {
    if (this.isDefault()) {
      throw new IllegalStateException("Categorias do sistema não podem ter alteração de tipo");
    }

    if (type == null) {
      throw new IllegalArgumentException("Tipo de categoria não pode estar vazio");
    }

    this.type = type;
  }
}

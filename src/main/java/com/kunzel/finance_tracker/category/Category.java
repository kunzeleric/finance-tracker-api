package com.kunzel.finance_tracker.category;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kunzel.finance_tracker.shared.HexColor;
import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;
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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(name = "uk_category_name", columnNames = { "name" })
})
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
  private String color;

  @Column(nullable = false)
  private Boolean isDefault;

  @Column(nullable = false)
  private LocalDate creationDate;

  @OneToMany(mappedBy = "category")
  @JsonManagedReference
  private List<Transaction> transactions;

  protected Category() {
  }

  private Category(String name, CategoryType type, String color, Boolean isDefault) {
    if (name == null || name.isBlank()) {
      throw new ValidationException("Nome da categoria não pode estar em branco");
    }

    if (type == null) {
      throw new ValidationException("Tipo da categoria é obrigatório");
    }

    this.name = name;
    this.type = type;
    this.color = HexColor.normalize(color);
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

  public String getColor() {
    return color;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }

  public void update(String name, String color, CategoryType type) {
    if (isDefault()) {
      if (name != null && !name.equals(this.name)) {
        throw new BusinessRuleException("Nome de categoria padrão não pode ser alterado");
      }

      if (type != null && type != this.type) {
        throw new BusinessRuleException("Tipo de categoria padrão não pode ser alterado");
      }
    }

    if (name != null && name.isBlank()) {
      throw new ValidationException("Nome da categoria não pode estar em branco");
    }

    String normalizedColor = color != null ? HexColor.normalize(color) : this.color;

    this.color = normalizedColor;

    if (!isDefault()) {
      if (name != null) {
        this.name = name;
      }

      if (type != null) {
        this.type = type;
      }
    }
  }

  public static Category createDefault(String name, CategoryType type, String color) {
    return new Category(name, type, color, true);
  }

  public static Category createCustom(String name, CategoryType type, String color) {
    return new Category(name, type, color, false);
  }
}

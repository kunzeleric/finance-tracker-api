package com.kunzel.finance_tracker.category;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;
import com.kunzel.finance_tracker.transaction.Transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

  @Column(nullable = false)
  private Boolean isDefault;

  @Column(nullable = false)
  private LocalDate creationDate;

  @OneToMany(mappedBy = "category")
  @JsonManagedReference
  private List<Transaction> transactions;

  protected Category() {
  }

  private Category(String name, Boolean isDefault) {
    if (name == null || name.isBlank()) {
      throw new ValidationException("Nome da categoria nao pode estar em branco");
    }

    this.name = name;
    this.isDefault = isDefault;
    this.creationDate = LocalDate.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }

  public void update(String name) {
    if (this.isDefault()) {
      throw new BusinessRuleException("Categorias do sistema não podem ter alteração de nome");
    }

    if (name == null || name.isBlank()) {
      throw new ValidationException("Nome de categoria não pode estar em branco");
    }

    this.name = name;
  }

  public static Category createDefault(String name) {
    return new Category(name, true);
  }

  public static Category createCustom(String name) {
    return new Category(name, false);
  }
}

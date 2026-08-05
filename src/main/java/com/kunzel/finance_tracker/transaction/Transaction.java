package com.kunzel.finance_tracker.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryType;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
  @SequenceGenerator(name = "transaction_seq", sequenceName = "transaction_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private LocalDate creationDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  @JsonBackReference
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  @JsonBackReference
  private Category category;

  protected Transaction() {
  }

  private Transaction(String description, BigDecimal amount, LocalDate date, Account account,
      Category category) {
    validate(description, amount, date, account, category);
    this.description = description;
    this.amount = amount;
    this.date = date;
    this.creationDate = LocalDate.now();
    this.account = account;
    this.category = category;
  }

  public Long getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public CategoryType getType() {
    return category.getType();
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public LocalDate getDate() {
    return date;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }

  public Account getAccount() {
    return account;
  }

  public Category getCategory() {
    return category;
  }

  private static void validate(String description, BigDecimal amount, LocalDate date,
      Account account,
      Category category) {
    if (description == null || description.isBlank()) {
      throw new ValidationException("Descrição da transação não pode estar em branco");
    }

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ValidationException("Valor da transação tem que ser positivo e maior que 0 (zero)");
    }
    if (date == null) {
      throw new ValidationException("Data da transação é obrigatória");
    }
    if (account == null) {
      throw new ValidationException("Conta da transação é obrigatória");
    }
    if (category == null) {
      throw new ValidationException("Categoria da transação é obrigatória");
    }
  }

  public static Transaction create(String description, BigDecimal amount, LocalDate date,
      Account account,
      Category category) {
    return new Transaction(description, amount, date, account, category);
  }

  public void update(String description, BigDecimal amount, LocalDate date, Account account,
      Category category) {
    validate(description, amount, date, account, category);
    this.description = description;
    this.amount = amount;
    this.date = date;
    this.account = account;
    this.category = category;
  }

  public BigDecimal getSignedAmount() {
    return this.getType() == CategoryType.EXPENSE
        ? this.amount.negate()
        : this.amount;
  }
}

package com.kunzel.finance_tracker.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.kunzel.finance_tracker.account.Account;
import com.kunzel.finance_tracker.category.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "transactions")
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
  @SequenceGenerator(name = "transaction_seq", sequenceName = "transaction_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  @NotNull(message = "Valor da transação não pode estar em branco.")
  @Positive(message = "Valor da transação tem que ser positivo e maior que 0 (zero)")
  BigDecimal amount;

  @Column(nullable = false)
  @NotNull(message = "Data da transação não pode estar em branco")
  LocalDate date;

  @Column(nullable = false)
  LocalDate creationDate;

  @ManyToOne()
  @JoinColumn(name = "account_id", nullable = false)
  @JsonBackReference
  private Account account;

  @ManyToOne()
  @JoinColumn(name = "category_id", nullable = false)
  @JsonBackReference
  private Category category;

  protected Transaction() {
  }

  private Transaction(BigDecimal amount, LocalDate date, Account account, Category category) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Valor da transação tem que ser positivo e maior que 0 (zero)");
    }
    if (date == null) {
      throw new IllegalArgumentException("Data da transação não pode estar em branco");
    }
    if (account == null) {
      throw new IllegalArgumentException("Conta da transação não pode estar em branco");
    }
    if (category == null) {
      throw new IllegalArgumentException("Categoria da transação não pode estar em branco");
    }

    this.amount = amount;
    this.date = date;
    this.creationDate = LocalDate.now();
    this.account = account;
    this.category = category;
  }

  public Long getId() {
    return id;
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

  public static Transaction create(BigDecimal amount, LocalDate date, Account account, Category category) {
    return new Transaction(amount, date, account, category);
  }
}

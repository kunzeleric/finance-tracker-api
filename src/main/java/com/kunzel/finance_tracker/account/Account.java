package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kunzel.finance_tracker.account.exceptions.InsufficientBalanceException;
import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;
import com.kunzel.finance_tracker.category.Category;
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
@Table(name = "accounts")
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
  @SequenceGenerator(name = "account_seq", sequenceName = "account_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private BigDecimal initialBalance;

  @Column(nullable = false)
  private BigDecimal currentBalance;

  @Column(nullable = false)
  private LocalDate creationDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountType type;

  @OneToMany(mappedBy = "account")
  @JsonManagedReference
  private List<Transaction> transactions;

  protected Account() {
  }

  private Account(String name, BigDecimal initialBalance, AccountType type) {
    this.name = name;
    this.initialBalance = initialBalance;
    this.currentBalance = initialBalance;
    this.type = type;
    this.creationDate = LocalDate.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getInitialBalance() {
    return initialBalance;
  }

  public BigDecimal getCurrentBalance() {
    return currentBalance;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }

  public AccountType getType() {
    return type;
  }

  public void changeType(AccountType newType) {
    if (newType == null) {
      throw new IllegalArgumentException("Tipo da conta inválido.");
    }
    // TODO: validar se é uma mudança permitida
    // TODO: registrar um log/auditoria da mudança
    this.type = newType;
  }

  public static Account create(String name, BigDecimal initialBalance, AccountType type) {
    if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidBalanceException(initialBalance);
    }

    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Nome da conta não pode estar em branco.");
    }

    if (type == null) {
      throw new IllegalArgumentException("Tipo da conta inválido.");
    }

    return new Account(name, initialBalance, type);
  }

  public void update(String name, AccountType type) {
    if (name != null) {
      if (name.isBlank()) {
        throw new IllegalArgumentException("Nome da conta não pode estar em branco");
      }
      this.name = name;
    }

    if (type != null) {
      changeType(type);
    }
  }

  public void deposit(BigDecimal amount) {
    validatePositiveAmount(amount, "Valor tem que ser positivo para ser depositado.");
    this.currentBalance = this.currentBalance.add(amount);
  }

  public void withdraw(BigDecimal amount) {
    validatePositiveAmount(amount, "Valor tem que ser positivo para ser sacado.");

    if (this.currentBalance.compareTo(amount) < 0) {
      throw new InsufficientBalanceException(amount);
    }

    this.currentBalance = this.currentBalance.subtract(amount);
  }

  private static void validatePositiveAmount(BigDecimal amount, String message) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(message);
    }
  }

  // Desfaz o efeito de uma transação existente (delete/update). Não valida.
  // Desfazer um EXPENSE, gera credito
  // Desfazer um INCOME, reduz o saldo
  public void reverseTransaction(Category category, BigDecimal amount) {
    if (category.isExpense()) {
      currentBalance = currentBalance.add(amount);
    } else {
      currentBalance = currentBalance.subtract(amount);
    }
  }

  // Aplica o efeito de uma transação nova/atualizada, sem validar saldo.
  public void applyTransaction(Category category, BigDecimal amount) {
    if (category.isExpense()) {
      currentBalance = currentBalance.subtract(amount);
    } else {
      currentBalance = currentBalance.add(amount);
    }
  }
}

package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kunzel.finance_tracker.account.exceptions.InsufficientBalanceException;
import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
  @SequenceGenerator(name = "account_seq", sequenceName = "account_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Nome da conta não pode estar vazio")
  private String name;

  @Column(nullable = false)
  @PositiveOrZero(message = "Saldo inicial não pode ser negativo")
  private BigDecimal balance;

  @Column(nullable = false)
  private LocalDate creationDate;

  @Column(nullable = false)
  @NotBlank(message = "Tipo de conta não pode estar vazio")
  private AccountType type;

  protected Account() {
  }

  public Account(String name, BigDecimal balance, AccountType type) {
    if (balance.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidBalanceException(balance);
    }

    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Nome da conta não pode estar vazio.");
    }

    if (type == null) {
      throw new IllegalArgumentException("Tipo da Conta inválido.");
    }

    this.name = name;
    this.balance = balance;
    this.type = type;
    this.creationDate = LocalDate.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }

  public AccountType getType() {
    return type;
  }

  public void deposit(BigDecimal amount) {
    validatePositiveAmount(amount, "Valor tem que ser positivo para ser depositado.");
    this.balance = this.balance.add(amount);
  }

  public void withdraw(BigDecimal amount) {
    validatePositiveAmount(amount, "Valor tem que ser positivo para ser sacado.");

    if (this.balance.compareTo(amount) < 0) {
      throw new InsufficientBalanceException(amount);
    }

    this.balance = this.balance.subtract(amount);
  }

  private static void validatePositiveAmount(BigDecimal amount, String message) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(message);
    }
  }
}

package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;
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
@Table(name = "accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_account_type", columnNames = { "name", "type" })
})
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

  // Aplica o efeito de uma transação nova/atualizada, sem validar saldo.
  public void applyTransaction(BigDecimal signedAmount) {
    this.currentBalance = this.currentBalance.add(signedAmount);
  }

  // Desfaz o efeito de uma transação existente (delete/update). Não valida.
  // Desfazer um EXPENSE, gera credito
  // Desfazer um INCOME, reduz o saldo
  public void reverseTransaction(BigDecimal signedAmount) {
    this.currentBalance = this.currentBalance.subtract(signedAmount);
  }

}

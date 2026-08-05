package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;
import com.kunzel.finance_tracker.shared.HexColor;
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

  @Column(nullable = false, updatable = false)
  private BigDecimal openingBalance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountType type;

  @Column(nullable = false)
  private String color;

  private String institution;

  @Column(nullable = false)
  private LocalDate creationDate;

  @OneToMany(mappedBy = "account")
  @JsonManagedReference
  private List<Transaction> transactions;

  protected Account() {
  }

  private Account(String name, BigDecimal openingBalance, AccountType type, String color, String institution) {
    this.name = name;
    this.openingBalance = openingBalance;
    this.type = type;
    this.color = HexColor.normalize(color);
    this.institution = institution;
    this.creationDate = LocalDate.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getOpeningBalance() {
    return openingBalance;
  }

  public AccountType getType() {
    return type;
  }

  public String getColor() {
    return color;
  }

  public String getInstitution() {
    return institution;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }

  public void changeType(AccountType newType) {
    if (newType == null) {
      throw new ValidationException("Tipo da conta é obrigatório");
    }
    // TODO: validar se é uma mudança permitida
    // TODO: registrar um log/auditoria da mudança
    this.type = newType;
  }

  public static Account create(String name, BigDecimal openingBalance, AccountType type, String color,
      String institution) {
    if (openingBalance == null || openingBalance.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidBalanceException(openingBalance);
    }

    if (name == null || name.isBlank()) {
      throw new ValidationException("Nome da conta não pode estar em branco");
    }

    if (type == null) {
      throw new ValidationException("Tipo da conta é obrigatório");
    }

    return new Account(name, openingBalance, type, color, institution);
  }

  public void update(String name, AccountType type, String color, String institution) {
    if (name != null) {
      if (name.isBlank()) {
        throw new ValidationException("Nome da conta não pode estar em branco");
      }
      this.name = name;
    }

    if (type != null) {
      changeType(type);
    }

    if (color != null) {
      this.color = HexColor.normalize(color);
    }

    if (institution != null) {
      this.institution = institution.isBlank() ? null : institution;
    }
  }
}

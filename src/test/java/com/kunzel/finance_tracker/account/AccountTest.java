package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kunzel.finance_tracker.account.exceptions.InvalidBalanceException;
import com.kunzel.finance_tracker.shared.HexColor;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;

import static org.assertj.core.api.Assertions.*;

class AccountTest {
  Account account;

  @BeforeEach
  void setup() {
    account = Account.create("Nubank Conta Corrente", BigDecimal.valueOf(1000.00), AccountType.CHECKING, "#7C3AED",
        "Nubank");
  }

  @Nested
  class Constructor {

    @Test
    void shouldCreateAccountWithValidOpeningBalance() {
      assertThat(account.getName()).isEqualTo("Nubank Conta Corrente");
      assertThat(account.getOpeningBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
      assertThat(account.getType()).isEqualTo(AccountType.CHECKING);
      assertThat(account.getColor()).isEqualTo("#7C3AED");
      assertThat(account.getInstitution()).isEqualTo("Nubank");
      assertThat(account.getCreationDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldFallBackToDefaultColorWhenColorIsNull() {
      Account withoutColor = Account.create("Poupança", BigDecimal.TEN, AccountType.SAVINGS, null, null);

      assertThat(withoutColor.getColor()).isEqualTo(HexColor.DEFAULT);
    }

    @Test
    void shouldAllowNullInstitution() {
      Account withoutInstitution = Account.create("Carteira", BigDecimal.TEN, AccountType.WALLET, null, null);

      assertThat(withoutInstitution.getInstitution()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenColorIsNotAValidHex() {
      assertThatThrownBy(() -> Account.create("Poupança", BigDecimal.TEN, AccountType.SAVINGS, "roxo", null))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenOpeningBalanceIsNegative() {
      assertThatThrownBy(() -> Account.create("Poupança", BigDecimal.valueOf(-10.00), AccountType.SAVINGS, null, null))
          .isInstanceOf(InvalidBalanceException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
      assertThatThrownBy(() -> Account.create("", BigDecimal.valueOf(10.00), AccountType.SAVINGS, null, null))
          .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionWhenAccountTypeIsEmpty() {
      assertThatThrownBy(() -> Account.create("Poupança", BigDecimal.valueOf(10.00), null, null, null))
          .isInstanceOf(ValidationException.class);
    }
  }

  @Nested
  class Update {

    @Test
    void shouldUpdateColorAndInstitution() {
      account.update(null, null, "#0EA5E9", "Inter");

      assertThat(account.getColor()).isEqualTo("#0EA5E9");
      assertThat(account.getInstitution()).isEqualTo("Inter");
    }

    @Test
    void shouldClearInstitutionWhenBlankIsGiven() {
      account.update(null, null, null, "  ");

      assertThat(account.getInstitution()).isNull();
    }

    @Test
    void shouldKeepFieldsWhenNullIsGiven() {
      account.update(null, null, null, null);

      assertThat(account.getName()).isEqualTo("Nubank Conta Corrente");
      assertThat(account.getColor()).isEqualTo("#7C3AED");
      assertThat(account.getInstitution()).isEqualTo("Nubank");
    }

    @Test
    void shouldNotExposeAnyWayToChangeOpeningBalance() {
      account.update("Outro nome", AccountType.SAVINGS, "#0EA5E9", "Inter");

      assertThat(account.getOpeningBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }
  }
}

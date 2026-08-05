package com.kunzel.finance_tracker.account;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
  Optional<Account> findExistingAccountByNameAndType(String name, AccountType type);

  @Query("SELECT COALESCE(SUM(a.openingBalance), 0) FROM Account a")
  BigDecimal sumOpeningBalances();
}

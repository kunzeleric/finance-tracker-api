package com.kunzel.finance_tracker.transaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  @Query("""
      SELECT t FROM Transaction t
      WHERE (:accountId IS NULL OR t.account.id = :accountId)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        AND (:startDate IS NULL OR t.date >= :startDate)
        AND (:endDate IS NULL OR t.date <= :endDate)
      """)
  List<Transaction> search(Long accountId, Long categoryId, LocalDate startDate, LocalDate endDate);
}

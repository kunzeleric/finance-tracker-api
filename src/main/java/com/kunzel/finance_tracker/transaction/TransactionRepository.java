package com.kunzel.finance_tracker.transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  @Query("""
      SELECT t FROM Transaction t JOIN FETCH t.category JOIN FETCH t.account
      WHERE (:accountId IS NULL OR t.account.id = :accountId)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        AND (:startDate IS NULL OR t.date >= :startDate)
        AND (:endDate IS NULL OR t.date <= :endDate)
      """)
  List<Transaction> search(Long accountId, Long categoryId, LocalDate startDate, LocalDate endDate);

  @Query("""
      SELECT t FROM Transaction t JOIN FETCH t.category JOIN FETCH t.account
      WHERE t.id = :transactionId
      """)
  Optional<Transaction> findByIdWithRelations(Long transactionId);

  boolean existsByCategoryId(Long categoryId);
}

package com.kunzel.finance_tracker.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kunzel.finance_tracker.category.Category;
import com.kunzel.finance_tracker.category.CategoryType;

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

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE Transaction t SET t.category = :target WHERE t.category.id = :sourceCategoryId")
  int reassignCategory(Long sourceCategoryId, Category target);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM Transaction t WHERE t.category.id = :categoryId")
  int deleteByCategoryId(Long categoryId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM Transaction t WHERE t.account.id = :accountId")
  int deleteByAccountId(Long accountId);

  @Query("""
      SELECT COALESCE(SUM(CASE WHEN t.category.type = :expenseType THEN -t.amount ELSE t.amount END), 0)
      FROM Transaction t
      WHERE (:accountId IS NULL OR t.account.id = :accountId)
      """)
  BigDecimal signedSum(Long accountId, CategoryType expenseType);

  @Query("""
      SELECT new com.kunzel.finance_tracker.transaction.AccountSignedSum(
        t.account.id,
        SUM(CASE WHEN t.category.type = :expenseType THEN -t.amount ELSE t.amount END))
      FROM Transaction t
      GROUP BY t.account.id
      """)
  List<AccountSignedSum> signedSumGroupedByAccount(CategoryType expenseType);
}

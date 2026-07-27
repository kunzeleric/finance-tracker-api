package com.kunzel.finance_tracker.transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findByCategoryId(Long categoryId);

  List<Transaction> findByAccountId(Long accountId);

  List<Transaction> findByAccountIdAndCategoryId(Long accountId, Long categoryId);
}

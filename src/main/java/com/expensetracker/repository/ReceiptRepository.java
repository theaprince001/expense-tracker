package com.expensetracker.repository;

import com.expensetracker.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByTransactionId(Long transactionId);
    List<Receipt> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByTransactionId(Long transactionId);
}
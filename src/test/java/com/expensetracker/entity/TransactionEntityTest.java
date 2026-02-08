package com.expensetracker.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityTest {

    @Test
    void transactionCreation_shouldWork() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setDate(LocalDate.now());

        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertNotNull(transaction.getDate());
    }
}
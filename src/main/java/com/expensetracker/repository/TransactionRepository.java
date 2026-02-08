package com.expensetracker.repository;

import com.expensetracker.dto.CategorySpending;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    boolean existsByCategoryId(Long categoryId);
    List<Transaction> findByUserIdAndDateBetweenAndTypeAndCategoryId(
            Long userId, LocalDate start, LocalDate end, TransactionType type, Long categoryId);


    List<Transaction> findByUserIdAndDateBetweenAndType(
            Long userId, LocalDate start, LocalDate end, TransactionType type);

    @Query("select t from Transaction t join fetch t.user")
    Page<Transaction> findAllWithUser(Pageable pageable);

    @Query("SELECT t.category.name, SUM(t.amount) as total, " +
            "(SUM(t.amount) * 100.0 / (SELECT SUM(t2.amount) FROM Transaction t2 " +
            "WHERE t2.user.id = :userId AND t2.date BETWEEN :start AND :end AND t2.type = :type)) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.date BETWEEN :start AND :end AND t.type = :type " +
            "GROUP BY t.category.name " +
            "ORDER BY total DESC")
    List<Object[]> findCategorySpendingBetweenDates(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("type") TransactionType type);

    List<Transaction> findTop10ByUserIdAndDateBetweenOrderByDateDesc(
            Long userId, LocalDate start, LocalDate end);

}

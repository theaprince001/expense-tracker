package com.expensetracker.repository;

import com.expensetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndMonthAndCategoryId(Long userId, YearMonth month, Long categoryId);
    List<Budget> findByUserIdOrderByMonthDesc(Long userId);
    List<Budget> findByUserIdAndMonth(Long userId, YearMonth month);


    @Query("select distinct b.user.id from Budget b where b.month = :month")
    List<Long> findDistinctUserIdsByMonth(@Param("month") YearMonth month);

}

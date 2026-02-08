package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
    List<Category> findByType(TransactionType type);
    List<Category> findByUserIdIsNull();

}

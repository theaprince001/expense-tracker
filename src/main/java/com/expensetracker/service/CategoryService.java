package com.expensetracker.service;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.TransactionType;

import java.util.List;

public interface CategoryService {

    List<Category> getUserCategories(Long userId);

    Category createCustomCategory(Category category, Long userId);

    Category getUserCategoryById(Long categoryId, Long userId);

    void validateCategoryForTransaction(Category category, Long userId, TransactionType type);

    Category updateUserCategory(Long categoryId, Long userId, Category category);
    void deleteUserCategory(Long categoryId, Long userId);


}

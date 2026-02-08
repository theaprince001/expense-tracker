package com.expensetracker.service;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BusinessRuleException;
import com.expensetracker.exception.ConflictException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.exception.UnauthorizedAccessException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<Category> getUserCategories(Long userId) {
        List<Category> result = new ArrayList<>();
        result.addAll(categoryRepository.findByUserIdIsNull());
        result.addAll(categoryRepository.findByUserId(userId));
        return result;
    }

    @Override
    public Category createCustomCategory(Category category, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        category.setUser(user);
        return categoryRepository.save(category);
    }

    @Override
    public Category getUserCategoryById(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Category does not belong to user");
        }
        return category;
    }

    @Override
    public void validateCategoryForTransaction(Category category, Long userId, TransactionType type) {
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Category does not belong to user");
        }
        if (!category.getType().equals(type)) {
            throw new BusinessRuleException("Category type must match transaction type");
        }
    }

    @Override
    @Transactional
    public Category updateUserCategory(Long categoryId, Long userId, Category updatedCategory) {

        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if (existingCategory.getUser() == null) {
            throw new UnauthorizedAccessException("Cannot update system category");
        }

        if (!existingCategory.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Category does not belong to user");
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setType(updatedCategory.getType());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setColor(updatedCategory.getColor());
        existingCategory.setIcon(updatedCategory.getIcon());

        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    public void deleteUserCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getUser() == null) {
            throw new UnauthorizedAccessException("Cannot delete system category");
        }

        if (!category.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Category does not belong to user");
        }

        boolean hasTransactions = transactionRepository.existsByCategoryId(categoryId);
        if (hasTransactions) {
            throw new BusinessRuleException("Cannot delete category with existing transactions");
        }

        categoryRepository.delete(category);
    }

}
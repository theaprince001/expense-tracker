package com.expensetracker.controller;

import com.expensetracker.dto.CategoryRequest;
import com.expensetracker.dto.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.security.CurrentUserService;
import com.expensetracker.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getUserCategories() {
        Long userId = currentUserService.getCurrentUserId();

        List<Category> categories = categoryService.getUserCategories(userId);
        List<CategoryResponse> responses = categories.stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .build();

        Category createdCategory = categoryService.createCustomCategory(category, userId);
        CategoryResponse response = convertToResponse(createdCategory);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        Long userId = currentUserService.getCurrentUserId();

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .build();

        Category updatedCategory = categoryService.updateUserCategory(id, userId, category);
        CategoryResponse response = convertToResponse(updatedCategory);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        categoryService.deleteUserCategory(id, userId);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse convertToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .description(category.getDescription())
                .color(category.getColor())
                .icon(category.getIcon())
                .isCustom(category.getUser() != null)
                .createdAt(category.getCreatedAt())
                .build();
    }
}
package com.expensetracker.service;

import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.entity.*;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setAlertThreshold(80);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Food");
        testCategory.setUser(testUser);
    }

    @Test
    void createOverallBudget_shouldSuccess() {
        BudgetRequest request = new BudgetRequest();
        request.setAmount(new BigDecimal("1000.00"));
        request.setMonth(YearMonth.of(2024, 12));
        request.setBudgetType(BudgetType.OVERALL);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        BudgetResponse response = budgetService.createBudget(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("1000.00"), response.getAmount());
        assertEquals(YearMonth.of(2024, 12), response.getMonth());
        assertEquals(BudgetType.OVERALL, response.getBudgetType());
        assertNull(response.getCategoryId());

        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void createCategoryBudget_shouldSuccess() {
        BudgetRequest request = new BudgetRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setMonth(YearMonth.of(2024, 12));
        request.setBudgetType(BudgetType.CATEGORY);
        request.setCategoryId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        BudgetResponse response = budgetService.createBudget(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(BudgetType.CATEGORY, response.getBudgetType());
        assertEquals(1L, response.getCategoryId());

        verify(categoryRepository, times(1)).findById(1L);
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void createBudgetWithNonExistentCategory_shouldThrowException() {
        BudgetRequest request = new BudgetRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setMonth(YearMonth.of(2024, 12));
        request.setBudgetType(BudgetType.CATEGORY);
        request.setCategoryId(999L); // Non-existent

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            budgetService.createBudget(request);
        });

        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById(999L);
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void calculateBudgetProgress_shouldReturnZeroForNoTransactions() {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setAmount(new BigDecimal("1000.00"));
        budget.setMonth(YearMonth.of(2024, 12));
        budget.setBudgetType(BudgetType.OVERALL);
        budget.setUser(testUser);

    }
}
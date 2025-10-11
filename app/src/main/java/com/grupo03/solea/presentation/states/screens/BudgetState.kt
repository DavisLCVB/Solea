package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.Status
import com.grupo03.solea.utils.BudgetError
import java.time.Instant

data class BudgetState(
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val error: BudgetError? = null
)

data class BudgetLimitsScreenState(
    val categoriesWithBudgets: List<Pair<Category, Budget?>> = emptyList(),
    val isLoading: Boolean = false,
    val error: BudgetError? = null
)

data class EditBudgetFormState(
    val selectedCategory: Category? = null,
    val existingBudget: Budget? = null,
    val budgetAmount: String = "",
    val selectedDate: Instant = Instant.now().plusSeconds(2592000), // 30 días
    val availableStatus: List<Status> = emptyList(),
    val isAmountValid: Boolean = true,
    val isLoading: Boolean = false,
    val error: BudgetError? = null,
    val successMessage: String? = null
)

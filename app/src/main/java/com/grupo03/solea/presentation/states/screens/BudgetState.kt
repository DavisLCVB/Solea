package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.Status
import com.grupo03.solea.utils.BudgetError
import java.time.Instant

/**
 * UI state for the budget list screen.
 *
 * Displays all budgets for the current user with loading and error states.
 *
 * @property budgets List of all budgets for the user
 * @property isLoading Whether budgets are being loaded
 * @property error Error that occurred during budget operations, null if no error
 */
data class BudgetState(
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val error: BudgetError? = null
)

/**
 * UI state for the budget limits screen.
 *
 * Shows categories with their associated budgets, allowing users to see
 * which categories have budget limits and which don't.
 *
 * @property categoriesWithBudgets List of categories paired with their budgets (null if no budget set)
 * @property isLoading Whether data is being loaded
 * @property error Error that occurred during operations, null if no error
 */
data class BudgetLimitsScreenState(
    val categoriesWithBudgets: List<Pair<Category, Budget?>> = emptyList(),
    val isLoading: Boolean = false,
    val error: BudgetError? = null
)

/**
 * UI state for the budget creation/edit form.
 *
 * Manages the state of creating a new budget or editing an existing one,
 * including form fields, validation, and operation status.
 *
 * @property selectedCategory Category for which the budget is being set
 * @property existingBudget Existing budget if editing, null if creating new
 * @property budgetAmount Budget amount as string (for text field binding)
 * @property selectedDate Expiration date for the budget (default: 30 days from now)
 * @property availableStatus List of possible budget statuses
 * @property isAmountValid Whether the entered amount is valid
 * @property isLoading Whether a save operation is in progress
 * @property error Error that occurred during save, null if no error
 * @property successMessage Success message to display, null if none
 */
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

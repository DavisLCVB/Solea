package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.repositories.interfaces.BudgetRepository
import com.grupo03.solea.data.repositories.interfaces.CategoryRepository
import com.grupo03.solea.presentation.states.screens.BudgetLimitsScreenState
import com.grupo03.solea.presentation.states.screens.BudgetState
import com.grupo03.solea.presentation.states.screens.EditBudgetFormState
import com.grupo03.solea.utils.BudgetError
import com.grupo03.solea.utils.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _budgetState = MutableStateFlow(BudgetState())
    val budgetState: StateFlow<BudgetState> = _budgetState.asStateFlow()

    private val _budgetLimitsScreenState = MutableStateFlow(BudgetLimitsScreenState())
    val budgetLimitsScreenState: StateFlow<BudgetLimitsScreenState> = _budgetLimitsScreenState.asStateFlow()

    private val _editBudgetFormState = MutableStateFlow(EditBudgetFormState())
    val editBudgetFormState: StateFlow<EditBudgetFormState> = _editBudgetFormState.asStateFlow()

    private var categories: List<Category> = emptyList()

    // Fetch budgets and categories
    fun fetchBudgetsAndCategories(userId: String) {
        viewModelScope.launch {
            _budgetLimitsScreenState.value = _budgetLimitsScreenState.value.copy(isLoading = true)

            // Fetch budgets
            val budgetsResult = budgetRepository.getAllBudgetsByUser(userId)
            val budgets = budgetsResult.getOrNull() ?: emptyList()
            _budgetState.value = _budgetState.value.copy(budgets = budgets)

            // Fetch categories (user + default)
            val userCategoriesResult = categoryRepository.getCategoriesByUser(userId)
            val defaultCategoriesResult = categoryRepository.getDefaultCategories()
            val userCategories = userCategoriesResult.getOrNull() ?: emptyList()
            val defaultCategories = defaultCategoriesResult.getOrNull() ?: emptyList()
            categories = userCategories + defaultCategories

            // Combine categories with budgets
            val categoriesWithBudgets = categories.map { category ->
                val budget = budgets.find { it.category == category.name }
                Pair(category, budget)
            }

            _budgetLimitsScreenState.value = _budgetLimitsScreenState.value.copy(
                categoriesWithBudgets = categoriesWithBudgets,
                isLoading = false
            )
        }
    }

    // Fetch available statuses
    fun fetchStatuses() {
        viewModelScope.launch {
            val statusesResult = budgetRepository.getAllStatus()
            val statuses = statusesResult.getOrNull() ?: emptyList()
            _editBudgetFormState.value = _editBudgetFormState.value.copy(
                availableStatus = statuses
            )
        }
    }

    // Select category for editing
    fun onSelectCategory(category: Category) {
        val budget = _budgetState.value.budgets.find { it.category == category.name }
        _editBudgetFormState.value = EditBudgetFormState(
            selectedCategory = category,
            existingBudget = budget,
            budgetAmount = budget?.amount?.toString() ?: "",
            selectedDate = budget?.until ?: Instant.now().plusSeconds(2592000),
            availableStatus = _editBudgetFormState.value.availableStatus
        )
    }

    // Update amount
    fun onAmountChange(newAmount: String) {
        val amount = newAmount.toDoubleOrNull()
        val isValid = amount != null && amount > 0
        _editBudgetFormState.value = _editBudgetFormState.value.copy(
            budgetAmount = newAmount,
            isAmountValid = isValid,
            error = if (!isValid && newAmount.isNotEmpty()) BudgetError.INVALID_AMOUNT else null
        )
    }

    // Update date
    fun onDateChange(newDate: Instant) {
        _editBudgetFormState.value = _editBudgetFormState.value.copy(
            selectedDate = newDate
        )
    }

    // Save budget (create or update)
    fun saveBudget(userId: String, onSuccess: () -> Unit) {
        val currentState = _editBudgetFormState.value
        val amount = currentState.budgetAmount.toDoubleOrNull()

        android.util.Log.d("BudgetViewModel", "saveBudget called with userId: $userId")
        android.util.Log.d("BudgetViewModel", "Current state: selectedCategory=${currentState.selectedCategory?.name}, amount=$amount, budgetAmount=${currentState.budgetAmount}")

        if (amount == null || amount <= 0) {
            android.util.Log.e("BudgetViewModel", "Invalid amount: $amount")
            _editBudgetFormState.value = currentState.copy(
                isAmountValid = false,
                error = BudgetError.INVALID_AMOUNT
            )
            return
        }

        if (currentState.selectedCategory == null) {
            android.util.Log.e("BudgetViewModel", "No category selected")
            _editBudgetFormState.value = currentState.copy(
                error = BudgetError.INVALID_CATEGORY
            )
            return
        }

        android.util.Log.d("BudgetViewModel", "Validation passed, setting loading state")
        _editBudgetFormState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val activeStatusId = currentState.availableStatus
                .find { it.value.equals("ACTIVE", ignoreCase = true) }?.id
                ?: "active"

            android.util.Log.d("BudgetViewModel", "Active status ID: $activeStatusId, available statuses: ${currentState.availableStatus.map { it.value }}")

            val result = if (currentState.existingBudget != null) {
                // Update existing budget
                android.util.Log.d("BudgetViewModel", "Updating existing budget: ${currentState.existingBudget.id}")
                val updatedBudget = currentState.existingBudget.copy(
                    amount = amount,
                    until = currentState.selectedDate,
                    statusId = activeStatusId
                )
                budgetRepository.updateBudget(updatedBudget)
            } else {
                // Create new budget
                val newBudget = Budget(
                    userId = userId,
                    category = currentState.selectedCategory.name,
                    amount = amount,
                    until = currentState.selectedDate,
                    statusId = activeStatusId
                )
                android.util.Log.d("BudgetViewModel", "Creating new budget: $newBudget")
                budgetRepository.createBudget(newBudget)
            }

            android.util.Log.d("BudgetViewModel", "Repository result: success=${result.isSuccess}, error=${result.errorOrNull()}")

            if (result.isSuccess) {
                android.util.Log.d("BudgetViewModel", "Budget saved successfully")
                _editBudgetFormState.value = EditBudgetFormState(
                    availableStatus = currentState.availableStatus,
                    successMessage = "Budget guardado exitosamente"
                )
                fetchBudgetsAndCategories(userId)
                onSuccess()
            } else {
                android.util.Log.e("BudgetViewModel", "Failed to save budget: ${result.errorOrNull()}")
                _editBudgetFormState.value = currentState.copy(
                    isLoading = false,
                    error = result.errorAs<BudgetError>() ?: BudgetError.UNKNOWN_ERROR
                )
            }
        }
    }

    // Delete budget
    fun deleteBudget(userId: String, budgetId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _editBudgetFormState.value = _editBudgetFormState.value.copy(isLoading = true)

            val result = budgetRepository.deleteBudget(budgetId)

            if (result.isSuccess) {
                _editBudgetFormState.value = EditBudgetFormState(
                    successMessage = "Budget eliminado exitosamente"
                )
                fetchBudgetsAndCategories(userId)
                onSuccess()
            } else {
                _editBudgetFormState.value = _editBudgetFormState.value.copy(
                    isLoading = false,
                    error = result.errorAs<BudgetError>() ?: BudgetError.UNKNOWN_ERROR
                )
            }
        }
    }

    fun calculateBudgetProgress(userId: String, movements: List<Movement>) {
        viewModelScope.launch {
            val budgets = _budgetState.value.budgets
            val now = Instant.now()

            val spendingByCategory = movements
                .filter { it.type == MovementType.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, movs) -> movs.sumOf { it.total } }

            val updatedBudgets = mutableListOf<Budget>()
            budgets.forEach { budget ->
                val spent = spendingByCategory[budget.category] ?: 0.0
                val percentage = if (budget.amount > 0) {
                    (spent / budget.amount) * 100
                } else {
                    0.0
                }

                val newStatusId = when {
                    budget.until.isBefore(now) -> "inactive"
                    percentage >= 100 -> "exceeded"
                    percentage >= 80 -> "warning"
                    else -> "active"
                }

                if (newStatusId != budget.statusId) {
                    val updatedBudget = budget.copy(statusId = newStatusId)
                    budgetRepository.updateBudget(updatedBudget)
                    updatedBudgets.add(updatedBudget)
                }
            }

            // Reload budgets if there were changes
            if (updatedBudgets.isNotEmpty()) {
                fetchBudgetsAndCategories(userId)
            }
        }
    }

    // Clear form
    fun clearForm() {
        _editBudgetFormState.value = EditBudgetFormState(
            availableStatus = _editBudgetFormState.value.availableStatus
        )
    }
}

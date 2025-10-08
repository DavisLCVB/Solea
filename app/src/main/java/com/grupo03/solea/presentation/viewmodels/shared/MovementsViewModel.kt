package com.grupo03.solea.presentation.viewmodels.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.repositories.interfaces.CategoryRepository
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.presentation.states.shared.MovementsState
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing financial movements (incomes and expenses) and categories.
 *
 * Provides both real-time observation and one-time fetching of movements and categories.
 * The real-time observation methods use Firestore listeners to automatically update the UI
 * when data changes, while the legacy fetch methods perform one-time queries.
 *
 * This ViewModel coordinates between MovementRepository and CategoryRepository to provide
 * a unified view of the user's financial data.
 *
 * @property movementRepository Repository for movement operations
 * @property categoryRepository Repository for category operations
 */
class MovementsViewModel(
    private val movementRepository: MovementRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    /** State containing all movements, categories, and error information */
    private val _movementsState = MutableStateFlow(MovementsState())
    val movementsState = _movementsState.asStateFlow()

    /**
     * Starts real-time observation of categories for a user.
     *
     * Sets up a Firestore listener that automatically updates the categories list
     * whenever categories are added, modified, or removed.
     *
     * @param userId The ID of the user whose categories to observe
     */
    fun observeCategories(userId: String) {
        viewModelScope.launch {
            categoryRepository.observeCategoriesByUser(userId).collect { result ->
                when (result) {
                    is RepositoryResult.Success -> {
                        _movementsState.value = _movementsState.value.copy(
                            categoriesList = result.data,
                            error = null
                        )
                    }
                    is RepositoryResult.Error -> {
                        _movementsState.value = _movementsState.value.copy(
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    /**
     * Starts real-time observation of movements (incomes and expenses) for a user.
     *
     * Sets up Firestore listeners for both incomes and expenses that automatically update
     * the movements lists whenever movements are added, modified, or removed.
     *
     * @param userId The ID of the user whose movements to observe
     */
    fun observeMovements(userId: String) {
        // Observe incomes
        viewModelScope.launch {
            movementRepository.observeIncomesByUserId(userId).collect { result ->
                when (result) {
                    is RepositoryResult.Success -> {
                        _movementsState.value = _movementsState.value.copy(
                            incomeDetailsList = result.data,
                            error = null
                        )
                    }
                    is RepositoryResult.Error -> {
                        _movementsState.value = _movementsState.value.copy(
                            error = result.error
                        )
                    }
                }
            }
        }

        // Observe expenses
        viewModelScope.launch {
            movementRepository.observeExpensesByUserId(userId).collect { result ->
                when (result) {
                    is RepositoryResult.Success -> {
                        _movementsState.value = _movementsState.value.copy(
                            expenseDetailsList = result.data,
                            error = null
                        )
                    }
                    is RepositoryResult.Error -> {
                        _movementsState.value = _movementsState.value.copy(
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    /**
     * Fetches categories for a user (legacy method).
     *
     * Performs a one-time query to get all categories. Prefer using [observeCategories]
     * for real-time updates.
     *
     * @param userId The ID of the user whose categories to fetch
     */
    fun fetchCategories(userId: String) {
        viewModelScope.launch {
            val categoriesResult = categoryRepository.getCategoriesByUser(userId)
            if (categoriesResult.isSuccess) {
                val categories = categoriesResult.getOrNull() ?: emptyList()
                _movementsState.value = _movementsState.value.copy(
                    categoriesList = categories,
                    error = null
                )
            } else {
                _movementsState.value = _movementsState.value.copy(
                    error = categoriesResult.errorOrNull()
                )
            }
        }
    }

    /**
     * Fetches movements for a user (legacy method).
     *
     * Performs one-time queries to get all incomes and expenses. Prefer using
     * [observeMovements] for real-time updates.
     *
     * @param userId The ID of the user whose movements to fetch
     */
    fun fetchMovements(userId: String) {
        viewModelScope.launch {
            // Fetch incomes and expenses
            val incomesResult = movementRepository.getIncomesByUserId(userId)
            val expensesResult = movementRepository.getExpensesByUserId(userId)

            if (!incomesResult.isSuccess) {
                _movementsState.value = _movementsState.value.copy(
                    error = incomesResult.errorOrNull()
                )
                return@launch
            }
            if (!expensesResult.isSuccess) {
                _movementsState.value = _movementsState.value.copy(
                    error = expensesResult.errorOrNull()
                )
                return@launch
            }

            val incomes = incomesResult.getOrNull() ?: emptyList()
            val expenses = expensesResult.getOrNull() ?: emptyList()

            // Update state with complete expense details
            _movementsState.value = _movementsState.value.copy(
                incomeDetailsList = incomes,
                expenseDetailsList = expenses,
                error = null
            )
        }
    }

    /**
     * Deletes a financial movement.
     *
     * Attempts to delete the movement with the given ID and invokes the appropriate
     * callback based on the result.
     *
     * @param movementId The ID of the movement to delete
     * @param onSuccess Callback invoked when deletion succeeds
     * @param onError Callback invoked when deletion fails, receives the error
     */
    fun deleteMovement(movementId: String, onSuccess: () -> Unit, onError: (com.grupo03.solea.utils.AppError) -> Unit) {
        viewModelScope.launch {
            val result = movementRepository.deleteMovement(movementId)
            when (result) {
                is RepositoryResult.Success -> {
                    onSuccess()
                }
                is RepositoryResult.Error -> {
                    onError(result.error)
                }
            }
        }
    }
}
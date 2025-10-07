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

class MovementsViewModel(
    private val movementRepository: MovementRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _movementsState = MutableStateFlow(MovementsState())
    val movementsState = _movementsState.asStateFlow()

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

    // Legacy fetch methods (keep for backward compatibility)
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
package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.data.repositories.interfaces.SavingsGoalRepository
import com.grupo03.solea.presentation.states.screens.SavingsState
import com.grupo03.solea.utils.AppError
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.SavingsGoalError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant

class SavingsViewModel(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val movementRepository: MovementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsState())
    val uiState = _uiState.asStateFlow()

    fun observeGoals(userUid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            savingsGoalRepository.observeGoalsByUser(userUid).collectLatest { result ->
                _uiState.value = when (result) {
                    is RepositoryResult.Success -> {
                        _uiState.value.copy(goals = result.data, isLoading = false, error = null)
                    }
                    is RepositoryResult.Error -> {
                        _uiState.value.copy(error = result.error, isLoading = false)
                    }
                }
            }
        }
    }

    fun addGoal(userUid: String, name: String, targetAmount: Double, deadline: Long) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                userId = userUid, // SavingsGoal uses userId, consistency is handled here.
                name = name,
                targetAmount = targetAmount,
                deadline = Instant.ofEpochMilli(deadline),
                createdAt = java.time.LocalDateTime.now()
            )
            savingsGoalRepository.createGoal(goal)
        }
    }

    fun addMoneyToGoal(userUid: String, goalId: String, amount: Double, currentBalance: Double) {
        viewModelScope.launch {
            // Validation 1: Cannot save more money than the total balance
            if (amount > currentBalance) {
                _uiState.value = _uiState.value.copy(error = SavingsGoalError.INVALID_AMOUNT)
                return@launch
            }

            val goal = _uiState.value.goals.find { it.id == goalId }
            if (goal != null) {
                // Validation 2: Cannot add money if goal is completed or inactive
                if (goal.isCompleted || !goal.isActive) return@launch

                // Create a saving movement
                val savingMovement = Movement(
                    userUid = userUid,
                    type = MovementType.SAVING,
                    name = "Ahorro para '${goal.name}'",
                    description = "Ahorro para meta",
                    total = amount,
                    category = "Ahorros"
                )
                movementRepository.createMovement(savingMovement)

                // Update the goal's current amount atomically
                savingsGoalRepository.updateCurrentAmount(goalId, amount)
            }
        }
    }

    fun markAsCompleted(goalId: String) {
        viewModelScope.launch {
            val goal = _uiState.value.goals.find { it.id == goalId }
            if (goal != null) {
                val updatedGoal = goal.copy(isCompleted = true, isActive = false)
                savingsGoalRepository.updateGoal(updatedGoal)
            }
        }
    }

    fun deactivateGoal(goalId: String) {
        viewModelScope.launch {
            val goal = _uiState.value.goals.find { it.id == goalId }
            if (goal != null) {
                val updatedGoal = goal.copy(isActive = false)
                savingsGoalRepository.updateGoal(updatedGoal)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
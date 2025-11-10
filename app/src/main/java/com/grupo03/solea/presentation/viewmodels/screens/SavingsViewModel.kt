package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.SavingsGoal
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.data.repositories.interfaces.SavingsGoalRepository
import com.grupo03.solea.presentation.states.screens.AddEditGoalFormState
import com.grupo03.solea.presentation.states.screens.SavingsState
import com.grupo03.solea.utils.AppError
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.SavingsGoalError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

class SavingsViewModel(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val movementRepository: MovementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsState())
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AddEditGoalFormState())
    val formState = _formState.asStateFlow()

    fun observeGoals(userUid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            savingsGoalRepository.observeGoalsByUser(userUid).collectLatest { result ->
                _uiState.update {
                    when (result) {
                        is RepositoryResult.Success -> it.copy(goals = result.data, isLoading = false, error = null)
                        is RepositoryResult.Error -> it.copy(error = result.error, isLoading = false)
                    }
                }
            }
        }
    }

    // --- Form State Management ---

    fun prepareFormForCreate() {
        _formState.value = AddEditGoalFormState()
    }

    fun prepareFormForEdit(goal: SavingsGoal) {
        _formState.value = AddEditGoalFormState(
            existingGoal = goal,
            name = goal.name,
            targetAmount = goal.targetAmount.toString(),
            deadline = goal.deadline
        )
    }

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name, isNameValid = name.isNotBlank()) }
    }

    fun onAmountChange(amount: String) {
        val amountAsDouble = amount.toDoubleOrNull()
        val isValid = amountAsDouble != null && amountAsDouble > 0
        _formState.update { it.copy(targetAmount = amount, isAmountValid = isValid) }
    }

    fun onDeadlineChange(deadline: Instant) {
        _formState.update { it.copy(deadline = deadline) }
    }

    // --- CRUD Operations using Form State ---

    fun saveGoal(userUid: String, onSuccess: () -> Unit) {
        val currentState = _formState.value
        if (!currentState.isNameValid || !currentState.isAmountValid || currentState.name.isBlank() || currentState.targetAmount.isBlank()) return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }

            val goalToSave = currentState.existingGoal?.copy(
                name = currentState.name,
                targetAmount = currentState.targetAmount.toDouble(),
                deadline = currentState.deadline
            ) ?: SavingsGoal(
                userId = userUid,
                name = currentState.name,
                targetAmount = currentState.targetAmount.toDouble(),
                deadline = currentState.deadline,
                createdAt = java.time.LocalDateTime.now()
            )

            val result = if (currentState.existingGoal != null) {
                savingsGoalRepository.updateGoal(goalToSave)
            } else {
                savingsGoalRepository.createGoal(goalToSave)
            }

            when (result) {
                is RepositoryResult.Success -> {
                    _formState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is RepositoryResult.Error -> {
                    _formState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }

    fun deleteGoal(onSuccess: () -> Unit) {
        val goalId = _formState.value.existingGoal?.id ?: return
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            
            // Primero eliminar todos los savings asociados al goal
            // Esto hará que el dinero "retorne" al balance (los movements se eliminan)
            val deleteSavingsResult = movementRepository.deleteSavingsByGoalId(goalId)
            if (deleteSavingsResult.isError) {
                _formState.update { 
                    it.copy(
                        isLoading = false, 
                        error = deleteSavingsResult.errorOrNull()
                    ) 
                }
                return@launch
            }
            
            // Luego eliminar el goal
            val result = savingsGoalRepository.deleteGoal(goalId)
            when (result) {
                is RepositoryResult.Success -> {
                    _formState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is RepositoryResult.Error -> {
                    _formState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
    // --- Existing functions ---
    fun addMoneyToGoal(userUid: String, goalId: String, amount: Double, currentBalance: Double) {
        viewModelScope.launch {
            if (amount > currentBalance) {
                _uiState.update { it.copy(error = SavingsGoalError.INVALID_AMOUNT) }
                return@launch
            }

            val goal = _uiState.value.goals.find { it.id == goalId } ?: return@launch
            if (goal.isCompleted || !goal.isActive) return@launch

            val movementId = UUID.randomUUID().toString()
            val movement = Movement(
                id = movementId,
                userUid = userUid,
                type = MovementType.SAVING,
                name = "Ahorro para '${goal.name}'",
                description = "Ahorro para meta",
                datetime = LocalDateTime.now(),
                currency = goal.currency,
                total = amount,
                category = "Ahorros",
                createdAt = LocalDateTime.now()
            )

            val save = com.grupo03.solea.data.models.Save(
                goalId = goalId,
                amount = amount
            )

            val result = movementRepository.createSaving(movement, save)

            if (result.isError) {
                _uiState.update { it.copy(error = result.errorOrNull()) }
                return@launch
            }

            // Actualiza el monto acumulado del goal
            savingsGoalRepository.updateCurrentAmount(goalId, amount)
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

    fun updateUiAfterAddingMoney(goalId: String, amount: Double) {
        _uiState.update { state ->
            state.copy(
                goals = state.goals.map { goal ->
                    if (goal.id == goalId)
                        goal.copy(currentAmount = goal.currentAmount + amount)
                    else goal
                }
            )
        }
    }


    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
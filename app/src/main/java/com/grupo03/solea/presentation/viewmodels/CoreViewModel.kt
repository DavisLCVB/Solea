package com.grupo03.solea.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.repositories.MovementsRepository
import com.grupo03.solea.presentation.states.CoreState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CoreViewModel(
    private val movementsRepository: MovementsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoreState.State())
    val uiState: StateFlow<CoreState.State> = _uiState.asStateFlow()

    private fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    private fun setMovements(movements: List<Movement>) {
        _uiState.value = _uiState.value.copy(movements = movements)
    }

    private fun setMovementTypes(movementTypes: List<MovementType>) {
        _uiState.value = _uiState.value.copy(movementTypes = movementTypes)
    }

    private fun setHomeScreenState(homeScreenState: CoreState.HomeScreenState) {
        _uiState.value = _uiState.value.copy(homeScreenState = homeScreenState)
    }

    fun onActivateSheet() {
        val currentState = _uiState.value.homeScreenState
        val newState = currentState.copy(activeSheet = true)
        _uiState.value = _uiState.value.copy(homeScreenState = newState)
    }

    fun onDeactivateSheet() {
        val currentState = _uiState.value.homeScreenState
        val newState = currentState.copy(activeSheet = false)
        _uiState.value = _uiState.value.copy(homeScreenState = newState)
    }

    fun fetchMovements(userId: String) {
        setLoading(true)
        viewModelScope.launch {
            val movements = movementsRepository.getAllMovements(userId)
            setMovements(movements)
            val movementTypeIds = movements.map { it.typeId }
            val movementTypes = movementsRepository.getAllMovementTypes(movementTypeIds)
            setMovementTypes(movementTypes)
            calculateHomeScreenState(movements)
            val movementSet = movements.mapNotNull { movement ->
                val type = movementTypes.find { it.id == movement.typeId }
                if (type != null) {
                    Pair(movement, type)
                } else {
                    null
                }
            }
            val currentState = _uiState.value.homeScreenState
            val newState = currentState.copy(movementSet = movementSet)
            _uiState.value = _uiState.value.copy(homeScreenState = newState)
            setLoading(false)
        }
    }

    private fun calculateHomeScreenState(movements: List<Movement>) {
        var balance = 0.0
        var income = 0.0
        var outcome = 0.0

        for (movement in movements) {
            if (movement.amount > 0) {
                income += movement.amount
                balance += movement.amount
            } else if (movement.amount < 0) {
                outcome += movement.amount
                balance -= movement.amount
            }
        }

        val homeScreenState = CoreState.HomeScreenState(
            balance = balance,
            income = income,
            outcome = outcome
        )
        setHomeScreenState(homeScreenState)
    }

}
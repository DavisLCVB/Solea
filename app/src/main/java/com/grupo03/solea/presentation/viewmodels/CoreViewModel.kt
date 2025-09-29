package com.grupo03.solea.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.repositories.MovementsRepository
import com.grupo03.solea.presentation.states.CoreState
import com.grupo03.solea.utils.ErrorCode
import com.grupo03.solea.utils.Validation
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

    private fun setNewMovementFormState(newMovementFormState: CoreState.NewMovementFormState) {
        _uiState.value = _uiState.value.copy(newMovementFormState = newMovementFormState)
    }

    private fun setNewMovementTypeFormState(newMovementTypeFormState: CoreState.NewMovementTypeFormState) {
        _uiState.value = _uiState.value.copy(newMovementTypeFormState = newMovementTypeFormState)
    }

    private fun setMovementError(errorCode: ErrorCode.Movement?, isTypeForm: Boolean = false) {
        if (isTypeForm) {
            val currentState = _uiState.value.newMovementTypeFormState
            val newState = currentState.copy(errorCode = errorCode)
            setNewMovementTypeFormState(newState)
        } else {
            val currentState = _uiState.value.newMovementFormState
            val newState = currentState.copy(errorCode = errorCode)
            setNewMovementFormState(newState)
        }
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
            val movementTypes = movementsRepository.getAllMovementTypesByUser(userId)
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


    fun onMovementTypeNameChange(newName: String) {
        val errorCode = Validation.checkName(newName)
        if (errorCode == null) {
            val currentState = _uiState.value.newMovementTypeFormState
            val newState = currentState.copy(typeName = newName)
            setNewMovementTypeFormState(newState)
        } else {
            val currentState = _uiState.value.newMovementTypeFormState
            val newState = currentState.copy(typeName = newName, isNameValid = false)
            setNewMovementTypeFormState(newState)
            setMovementError(ErrorCode.Movement.INVALID_NAME, isTypeForm = true)
        }
    }

    fun onMovementTypeDescriptionChange(newDescription: String) {
        val currentState = _uiState.value.newMovementTypeFormState
        val newState = currentState.copy(typeDescription = newDescription)
        setNewMovementTypeFormState(newState)
    }

    fun onMovementAmountChange(newAmount: String) {
        val amount = newAmount.toDoubleOrNull()
        if (amount != null && amount != 0.0) {
            val currentState = _uiState.value.newMovementFormState
            val newState = currentState.copy(movementAmount = newAmount, isAmountValid = true)
            setNewMovementFormState(newState)
        } else {
            val currentState = _uiState.value.newMovementFormState
            val newState = currentState.copy(movementAmount = newAmount, isAmountValid = false)
            setNewMovementFormState(newState)
            setMovementError(ErrorCode.Movement.INVALID_AMOUNT)
        }
    }

    fun onMovementTypeSelected(newType: String) {
        val currentState = _uiState.value.newMovementFormState
        val newState = currentState.copy(typeSelected = newType, isTypeValid = true)
        setNewMovementFormState(newState)
    }

    fun onMovementNoteChange(newNote: String) {
        val currentState = _uiState.value.newMovementFormState
        val newState = currentState.copy(note = newNote)
        setNewMovementFormState(newState)
    }

    fun createMovement(userId: String) {
        val currentState = _uiState.value.newMovementFormState
        val amount = currentState.movementAmount.toDoubleOrNull()
        val type = _uiState.value.movementTypes.find { it.value == currentState.typeSelected }
        val note = currentState.note

        var isValid = true
        if (amount == null || amount == 0.0) {
            isValid = false
            setMovementError(ErrorCode.Movement.INVALID_AMOUNT)
        }
        if (type == null) {
            isValid = false
            setMovementError(ErrorCode.Movement.INVALID_TYPE)
        }

        if (!isValid) {
            return
        }

        setLoading(true)
        viewModelScope.launch {
            val movement = Movement(
                id = "",
                userId = userId,
                amount = amount!!,
                typeId = type!!.id,
                note = note
            )
            val success = movementsRepository.createMovement(movement)
            if (success != null) {
                fetchMovements(userId)
            } else {
                setMovementError(ErrorCode.Movement.UNKNOWN_ERROR)
            }
            setLoading(false)
            changeContent(CoreState.HomeContent.HOME)
        }
    }

    fun createMovementType(userId: String) {
        val currentState = _uiState.value.newMovementTypeFormState
        val name = currentState.typeName
        val description = currentState.typeDescription

        var isValid = true
        val nameError = Validation.checkName(name)
        if (nameError != null) {
            isValid = false
            setMovementError(ErrorCode.Movement.INVALID_NAME, isTypeForm = true)
        }

        if (!isValid) {
            return
        }

        setLoading(true)
        viewModelScope.launch {
            val movementType = MovementType(
                id = "",
                userId = userId,
                value = name,
                description = description
            )
            val success = movementsRepository.createMovementType(movementType)
            if (success != null) {
                fetchMovements(userId)
            } else {
                setMovementError(ErrorCode.Movement.UNKNOWN_ERROR, isTypeForm = true)
            }
            setLoading(false)
            changeContent(CoreState.HomeContent.HOME)
        }
    }

    fun changeContent(newContent: CoreState.HomeContent) {
        _uiState.value = _uiState.value.copy(currentContent = newContent)
    }

}
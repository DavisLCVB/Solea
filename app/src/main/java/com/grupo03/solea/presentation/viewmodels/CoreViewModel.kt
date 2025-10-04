package com.grupo03.solea.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.repositories.BudgetsRepository
import com.grupo03.solea.data.repositories.MovementsRepository
import com.grupo03.solea.presentation.states.CoreState
import com.grupo03.solea.utils.ErrorCode
import com.grupo03.solea.utils.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.math.abs

class CoreViewModel(
    private val movementsRepository: MovementsRepository,
    private val budgetsRepository: BudgetsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoreState.State())
    val uiState: StateFlow<CoreState.State> = _uiState.asStateFlow()

    private fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    private fun setMovements(movements: List<Movement>) { //List<ErrorCode.Movement>
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

            // AGREGADO
            calculateBudgetProgress(userId)

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
                calculateBudgetProgress(userId)  // calculo del budget
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

    // Funciones del budget limit

    private fun setBudgets(budgets: List<Budget>) {
        _uiState.value = _uiState.value.copy(budgets = budgets)
    }

    private fun setBudgetLimitsState(budgetLimitsState: CoreState.BudgetLimitsState) {
        _uiState.value = _uiState.value.copy(budgetLimitsState = budgetLimitsState)
    }

    private fun setBudgetError(errorCode: ErrorCode.Budget?) {
        val currentState = _uiState.value.budgetLimitsState
        val newState = currentState.copy(errorCode = errorCode)
        setBudgetLimitsState(newState)
    }

    fun fetchBudgets(userId: String) {
        viewModelScope.launch {
            val budgets = budgetsRepository.getAllBudgetsByUser(userId)
            setBudgets(budgets)
            updateBudgetLimitsWithData(userId)
        }
    }

    private suspend fun updateBudgetLimitsWithData(userId: String) {
        val movementTypes = _uiState.value.movementTypes
        val budgets = _uiState.value.budgets

        val categoriesWithBudgets = movementTypes.map { type ->
            val budget = budgets.find { it.movementTypeId == type.id }
            Pair(type, budget)
        }

        val currentState = _uiState.value.budgetLimitsState
        val newState = currentState.copy(categoriesWithBudgets = categoriesWithBudgets)
        setBudgetLimitsState(newState)
    }

    fun onSelectCategoryForBudget(category: MovementType) {
        val currentState = _uiState.value.budgetLimitsState
        val budget = _uiState.value.budgets.find { it.movementTypeId == category.id }
        val newState = currentState.copy(
            selectedCategory = category,
            budgetAmount = budget?.amount?.toString() ?: ""
        )
        setBudgetLimitsState(newState)
    }

    fun onBudgetAmountChange(newAmount: String) {
        val amount = newAmount.toDoubleOrNull()
        if (amount != null && amount > 0) {
            val currentState = _uiState.value.budgetLimitsState
            val newState = currentState.copy(budgetAmount = newAmount, isAmountValid = true)
            setBudgetLimitsState(newState)
        } else {
            val currentState = _uiState.value.budgetLimitsState
            val newState = currentState.copy(budgetAmount = newAmount, isAmountValid = false)
            setBudgetLimitsState(newState)
            setBudgetError(ErrorCode.Budget.INVALID_AMOUNT)
        }
    }

    fun saveBudgetLimit(userId: String, statusId: String, untilDate: Instant) {
        val currentState = _uiState.value.budgetLimitsState
        val category = currentState.selectedCategory
        val amount = currentState.budgetAmount.toDoubleOrNull()

        if (category == null || amount == null || amount <= 0) {
            setBudgetError(ErrorCode.Budget.INVALID_AMOUNT)
            return
        }

        setLoading(true)
        viewModelScope.launch {
            val existingBudget = _uiState.value.budgets.find { it.movementTypeId == category.id }

            val success = if (existingBudget != null) {
                val updatedBudget = existingBudget.copy(
                    amount = amount,
                    until = untilDate,
                    statusId = statusId
                )
                budgetsRepository.updateBudget(updatedBudget)
            } else {
                val newBudget = Budget(
                    id = "",
                    userId = userId,
                    amount = amount,
                    movementTypeId = category.id,
                    statusId = statusId,
                    until = untilDate
                )
                budgetsRepository.createBudget(newBudget) != null
            }

            if (success) {
                fetchBudgets(userId)
                // Resetear selección después de guardar
                val resetState = CoreState.BudgetLimitsState(
                    categoriesWithBudgets = _uiState.value.budgetLimitsState.categoriesWithBudgets
                )
                setBudgetLimitsState(resetState)
                changeSettingsContent(CoreState.SettingsContent.SETTINGS)
            } else {
                setBudgetError(ErrorCode.Budget.UNKNOWN_ERROR)
            }
            setLoading(false)
        }
    }
    fun deleteBudgetLimit(userId: String, budgetId: String) {
        setLoading(true)
        viewModelScope.launch {
            val success = budgetsRepository.deleteBudget(budgetId)
            if (success) {
                fetchBudgets(userId)
            }
            setLoading(false)
        }
    }

    fun cancelBudgetEdit() {
        val currentState = _uiState.value.budgetLimitsState
        val resetState = currentState.copy(
            selectedCategory = null,
            budgetAmount = "",
            isAmountValid = true,
            errorCode = null
        )
        setBudgetLimitsState(resetState)
    }
    fun changeSettingsContent(newContent: CoreState.SettingsContent) {
        // Resetear el estado de budget limits cuando volvemos a SETTINGS
        if (newContent == CoreState.SettingsContent.SETTINGS) {
            val resetState = CoreState.BudgetLimitsState()
            setBudgetLimitsState(resetState)
        }
        _uiState.value = _uiState.value.copy(currentSettingsContent = newContent)
    }

    fun calculateBudgetProgress(userId: String) {
        viewModelScope.launch {
            val movements = _uiState.value.movements
            val budgets = _uiState.value.budgets

            // Calcular gasto por categoría (solo movimientos negativos)
            val spendingByCategory = movements
                .filter { it.amount < 0 } // Solo gastos
                .groupBy { it.typeId }
                .mapValues { entry ->
                    kotlin.math.abs(entry.value.sumOf { it.amount })
                }

            val now = Instant.now()

            // Actualizar status de cada budget
            val updatedBudgets = mutableListOf<Budget>()
            budgets.forEach { budget ->
                val spent = spendingByCategory[budget.movementTypeId] ?: 0.0
                val percentage = if (budget.amount > 0) {
                    (spent / budget.amount) * 100
                } else 0.0

                // Determinar nuevo status
                val newStatusId = when {
                    budget.until.isBefore(now) -> "inactive"
                    percentage >= 100 -> "exceeded"
                    percentage >= 80 -> "warning"
                    else -> "active"
                }

                // Solo actualizar si cambió el status
                if (newStatusId != budget.statusId) {
                    val updatedBudget = budget.copy(statusId = newStatusId)
                    budgetsRepository.updateBudget(updatedBudget)
                    updatedBudgets.add(updatedBudget)
                }
            }

            // Recargar budgets actualizados si hubo cambios
            if (updatedBudgets.isNotEmpty()) {
                fetchBudgets(userId)
            }
        }
    }

}
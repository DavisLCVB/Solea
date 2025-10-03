package com.grupo03.solea.presentation.viewmodels

import android.util.Log
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
    private val movementsRepository: MovementsRepository,
    private val receiptAnalysisRepository: com.grupo03.solea.data.repositories.IReceiptAnalysisRepository? = null
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

    private fun setReceiptCameraState(receiptCameraState: CoreState.ReceiptCameraState) {
        _uiState.value = _uiState.value.copy(receiptCameraState = receiptCameraState)
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

    fun createMovementFromReceipt(userId: String) {
        val currentState = _uiState.value.receiptCameraState
        val analysisResult = currentState.analysisResult ?: return
        
        viewModelScope.launch {
            try {
                // Buscar o crear tipo de movimiento "Compras"
                val existingShoppingType = _uiState.value.movementTypes.find { 
                    it.value.equals("Compras", ignoreCase = true) || 
                    it.value.equals("Shopping", ignoreCase = true) ||
                    it.value.equals("Gastos", ignoreCase = true)
                }
                
                val movementTypeId = if (existingShoppingType != null) {
                    existingShoppingType.id
                } else {
                    // Crear nuevo tipo "Compras"
                    val newType = MovementType(
                        id = "",
                        userId = userId,
                        value = "Compras",
                        description = "Gastos en tiendas y supermercados"
                    )
                    val createdType = movementsRepository.createMovementType(newType)
                    createdType?.id ?: run {
                        setReceiptCameraState(
                            currentState.copy(
                                errorMessage = "Error al crear el tipo de movimiento"
                            )
                        )
                        return@launch
                    }
                }
                
                val storeName = analysisResult.computedStoreInfo?.name ?: "Tienda"
                val items = analysisResult.computedItems
                
                if (items.isNullOrEmpty()) {
                    // Si no hay items específicos, crear un movimiento con el total
                    val totalAmount = analysisResult.computedTotals?.totalPrinted ?: 0.0
                    if (totalAmount > 0) {
                        val movement = Movement(
                            id = "",
                            userId = userId,
                            amount = -totalAmount, // Negativo porque es un gasto
                            typeId = movementTypeId,
                            note = "Compra en $storeName",
                            item = "Compra general"
                        )
                        
                        movementsRepository.createMovement(movement)
                    }
                } else {
                    // Crear un movimiento por cada item
                    var successCount = 0
                    
                    items.forEach { item ->
                        val itemPrice = item.computedTotal ?: item.unitPrice ?: 0.0
                        if (itemPrice > 0) {
                            val itemName = item.description?.take(50) ?: "Producto"
                            val quantity = item.quantity ?: 1.0
                            val unit = item.unit ?: ""
                            
                            val note = buildString {
                                append("$storeName")
                                if (quantity > 1) {
                                    append(" - $quantity")
                                    if (unit.isNotEmpty()) append(" $unit")
                                }
                            }
                            
                            val movement = Movement(
                                id = "",
                                userId = userId,
                                amount = -itemPrice, // Negativo porque es un gasto
                                typeId = movementTypeId,
                                note = note,
                                item = itemName
                            )
                            
                            val success = movementsRepository.createMovement(movement)
                            if (success != null) {
                                successCount++
                            }
                        }
                    }
                    
                    Log.d("CoreViewModel", "Created $successCount movements from ${items.size} items")
                }
                
                // Actualizar la lista de movimientos
                fetchMovements(userId)
                
                // Limpiar el estado de la cámara
                setReceiptCameraState(CoreState.ReceiptCameraState())
                
                // Volver a la pantalla principal
                changeContent(CoreState.HomeContent.HOME)
                
            } catch (e: Exception) {
                Log.e("CoreViewModel", "Error creating movements from receipt", e)
                setReceiptCameraState(
                    currentState.copy(
                        errorMessage = "Error al crear los movimientos: ${e.message}"
                    )
                )
            }
        }
    }

    fun analyzeReceiptImage(imageUri: android.net.Uri, context: android.content.Context) {
        Log.d("CoreViewModel", "Starting receipt analysis for URI: $imageUri")
        
        viewModelScope.launch {
            if (receiptAnalysisRepository == null) {
                Log.e("CoreViewModel", "Receipt analysis repository is null")
                setReceiptCameraState(
                    CoreState.ReceiptCameraState(
                        isLoading = false,
                        errorMessage = "Servicio de análisis no disponible"
                    )
                )
                return@launch
            }

            Log.d("CoreViewModel", "Setting loading state to true")
            setReceiptCameraState(
                CoreState.ReceiptCameraState(isLoading = true)
            )

            try {
                Log.d("CoreViewModel", "Calling repository analyzeReceiptImage")
                val result = receiptAnalysisRepository.analyzeReceiptImage(imageUri, context)
                
                result.fold(
                    onSuccess = { analysisResult ->
                        Log.d("CoreViewModel", "Analysis successful")
                        Log.d("CoreViewModel", "Store: ${analysisResult.computedStoreInfo?.name}")
                        Log.d("CoreViewModel", "Total: ${analysisResult.computedTotals?.totalPrinted}")
                        setReceiptCameraState(
                            CoreState.ReceiptCameraState(
                                isLoading = false,
                                analysisResult = analysisResult
                            )
                        )
                        // El movimiento se creará cuando el usuario presione el botón
                    },
                    onFailure = { error ->
                        Log.e("CoreViewModel", "Analysis failed", error)
                        setReceiptCameraState(
                            CoreState.ReceiptCameraState(
                                isLoading = false,
                                errorMessage = error.message ?: "Error desconocido"
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("CoreViewModel", "Exception during analysis", e)
                setReceiptCameraState(
                    CoreState.ReceiptCameraState(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}\nTipo: ${e.javaClass.simpleName}"
                    )
                )
            }
        }
    }

    fun clearReceiptCameraError() {
        val currentState = _uiState.value.receiptCameraState
        setReceiptCameraState(currentState.copy(errorMessage = null))
    }

}
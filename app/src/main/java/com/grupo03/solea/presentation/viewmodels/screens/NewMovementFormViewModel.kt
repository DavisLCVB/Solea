package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.Expense
import com.grupo03.solea.data.models.Income
import com.grupo03.solea.data.models.Item
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.Receipt
import com.grupo03.solea.data.models.Source
import com.grupo03.solea.data.models.SourceType
import com.grupo03.solea.data.repositories.interfaces.CategoryRepository
import com.grupo03.solea.data.repositories.interfaces.ItemRepository
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.data.repositories.interfaces.ReceiptRepository
import com.grupo03.solea.data.repositories.interfaces.ShoppingListRepository
import com.grupo03.solea.data.services.interfaces.AuthService
import com.grupo03.solea.presentation.states.screens.NewMovementFormState
import com.grupo03.solea.presentation.states.screens.ReceiptItemData
import com.grupo03.solea.utils.CurrencyUtils
import com.grupo03.solea.utils.MovementError
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class NewMovementFormViewModel(
    private val authService: AuthService,
    private val movementRepository: MovementRepository,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: ItemRepository,
    private val receiptRepository: ReceiptRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(
        NewMovementFormState(currency = CurrencyUtils.getCurrencyByCountry())
    )
    val formState: StateFlow<NewMovementFormState> = _formState.asStateFlow()

    init {
        // Obtener currency del usuario actual
        viewModelScope.launch {
            val user = authService.getCurrentUser()
            val currency = user?.currency ?: CurrencyUtils.getCurrencyByCountry()
            _formState.value = _formState.value.copy(currency = currency)
        }
    }

    fun fetchCategories(userId: String) {
        viewModelScope.launch {
            val userCategoriesResult = categoryRepository.getCategoriesByUser(userId)
            val defaultCategoriesResult = categoryRepository.getDefaultCategories()

            val userCategories = userCategoriesResult.getOrNull() ?: emptyList()
            val defaultCategories = defaultCategoriesResult.getOrNull() ?: emptyList()

            val allCategories = userCategories + defaultCategories
            _formState.value = _formState.value.copy(categories = allCategories)
        }
    }

    fun onNameChange(newName: String) {
        val isValid = newName.isNotBlank()
        _formState.value = _formState.value.copy(
            name = newName,
            isNameValid = isValid,
            error = if (!isValid && newName.isNotEmpty()) MovementError.INVALID_TYPE else null
        )
    }

    fun onDescriptionChange(newDescription: String) {
        _formState.value = _formState.value.copy(description = newDescription)
    }

    fun onAmountChange(newAmount: String) {
        val amount = newAmount.toDoubleOrNull()
        val isValid = amount != null && amount > 0
        _formState.value = _formState.value.copy(
            amount = newAmount,
            isAmountValid = isValid,
            error = if (!isValid && newAmount.isNotEmpty()) MovementError.INVALID_AMOUNT else null
        )
    }

    fun onCategorySelected(category: Category?) {
        _formState.value = _formState.value.copy(
            selectedCategory = category,
            isCategorySelected = true,
            error = null
        )
    }

    /**
     * Pre-fills the form with data from a shopping item.
     * Forces movement type to EXPENSE and locks it.
     */
    fun prefillFromShoppingItem(itemId: String, itemName: String, estimatedPrice: Double?) {
        _formState.value = _formState.value.copy(
            movementType = MovementType.EXPENSE,
            name = itemName,
            itemName = itemName,
            amount = estimatedPrice?.toString() ?: "",
            itemUnitPrice = estimatedPrice?.toString() ?: "",
            itemQuantity = "1.0",
            sourceType = SourceType.ITEM,
            shoppingItemId = itemId,
            isNameValid = itemName.isNotBlank(),
            isAmountValid = estimatedPrice != null && estimatedPrice > 0
        )
    }

    fun onMovementTypeChange(newType: MovementType) {
        _formState.value = _formState.value.copy(movementType = newType)
    }

    fun onSourceTypeChange(newSourceType: SourceType) {
        _formState.value = _formState.value.copy(sourceType = newSourceType)
    }

    fun onItemNameChange(newItemName: String) {
        _formState.value = _formState.value.copy(itemName = newItemName)
    }

    fun onItemQuantityChange(newQuantity: String) {
        _formState.value = _formState.value.copy(itemQuantity = newQuantity)
    }

    fun onItemUnitPriceChange(newUnitPrice: String) {
        _formState.value = _formState.value.copy(itemUnitPrice = newUnitPrice)
    }

    fun onReceiptDescriptionChange(newDescription: String) {
        _formState.value = _formState.value.copy(receiptDescription = newDescription)
    }

    fun addReceiptItem() {
        val newItem = ReceiptItemData()
        _formState.value = _formState.value.copy(
            receiptItems = _formState.value.receiptItems + newItem
        )
    }

    fun removeReceiptItem(index: Int) {
        val updatedItems = _formState.value.receiptItems.toMutableList()
        updatedItems.removeAt(index)
        _formState.value = _formState.value.copy(receiptItems = updatedItems)
    }

    fun updateReceiptItem(index: Int, item: ReceiptItemData) {
        val updatedItems = _formState.value.receiptItems.toMutableList()
        updatedItems[index] = item
        _formState.value = _formState.value.copy(receiptItems = updatedItems)
    }

    fun onGoalSelected(goalId: String) {
        _formState.value = _formState.value.copy(selectedGoalId = goalId)
    }

    fun onDateTimeChange(dateTime: LocalDateTime?) {
        _formState.value = _formState.value.copy(datetime = dateTime)
    }
    fun createMovement(userId: String, onSuccess: () -> Unit) {
        val currentState = _formState.value

        if (currentState.name.isBlank()) {
            _formState.value = currentState.copy(
                isNameValid = false,
                error = MovementError.INVALID_TYPE
            )
            return
        }

        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _formState.value = currentState.copy(
                isAmountValid = false,
                error = MovementError.INVALID_AMOUNT
            )
            return
        }

        if (currentState.movementType == MovementType.EXPENSE && currentState.selectedCategory == null) {
            _formState.value = currentState.copy(
                isCategorySelected = false,
                error = MovementError.INVALID_TYPE
            )
            return
        }

        if (currentState.movementType == MovementType.EXPENSE) {
            when (currentState.sourceType) {
                SourceType.ITEM -> {
                    val quantity = currentState.itemQuantity.toDoubleOrNull()
                    val unitPrice = currentState.itemUnitPrice.toDoubleOrNull()

                    if (currentState.itemName.isBlank() || quantity == null || quantity <= 0 || unitPrice == null || unitPrice <= 0) {
                        _formState.value = currentState.copy(
                            isSourceValid = false,
                            error = MovementError.INVALID_AMOUNT
                        )
                        return
                    }
                }

                SourceType.RECEIPT -> {
                    if (currentState.receiptItems.isEmpty()) {
                        _formState.value = currentState.copy(
                            isSourceValid = false,
                            error = MovementError.INVALID_AMOUNT
                        )
                        return
                    }
                    for (receiptItem in currentState.receiptItems) {
                        val qty = receiptItem.quantity.toDoubleOrNull()
                        val price = receiptItem.unitPrice.toDoubleOrNull()
                        if (receiptItem.name.isBlank() || qty == null || qty <= 0 || price == null || price <= 0) {
                            _formState.value = currentState.copy(
                                isSourceValid = false,
                                error = MovementError.INVALID_AMOUNT
                            )
                            return
                        }
                    }
                }
            }
        }

        _formState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val movementId = UUID.randomUUID().toString()
            val movementDateTime = currentState.datetime ?: LocalDateTime.now()
            val movement = Movement(
                id = movementId,
                userUid = userId,
                type = currentState.movementType,
                name = currentState.name,
                description = currentState.description,
                datetime = movementDateTime,
                currency = currentState.currency,
                total = amount,
                category = if (currentState.movementType == MovementType.EXPENSE) currentState.selectedCategory?.name else null,
                createdAt = LocalDateTime.now()
            )

            val movementResult = movementRepository.createMovement(movement)

            if (movementResult.isError) {
                _formState.value = currentState.copy(
                    isLoading = false,
                    error = movementResult.errorOrNull()
                )
                return@launch
            }

            when (currentState.movementType) {
                MovementType.INCOME -> {
                    val income = Income(
                        id = UUID.randomUUID().toString(),
                        movementId = movementId
                    )
                    val incomeResult = movementRepository.createIncome(income)

                    if (incomeResult.isError) {
                        _formState.value = currentState.copy(
                            isLoading = false,
                            error = incomeResult.errorOrNull()
                        )
                        return@launch
                    }
                }

                MovementType.EXPENSE -> {
                    when (currentState.sourceType) {
                        SourceType.ITEM -> {
                            val itemId = UUID.randomUUID().toString()
                            val item = Item(
                                id = itemId,
                                receiptId = null,
                                description = currentState.itemName,
                                quantity = currentState.itemQuantity.toDouble(),
                                currency = currentState.currency,
                                unitPrice = currentState.itemUnitPrice.toDouble(),
                                category = currentState.selectedCategory?.name ?: "",
                                createdAt = LocalDateTime.now()
                            )

                            val itemResult = itemRepository.createItem(item)
                            if (itemResult.isError) {
                                _formState.value = currentState.copy(
                                    isLoading = false,
                                    error = itemResult.errorOrNull()
                                )
                                return@launch
                            }

                            val sourceId = UUID.randomUUID().toString()
                            val source = Source(
                                id = sourceId,
                                sourceType = SourceType.ITEM,
                                sourceItemId = itemId,
                                sourceReceiptId = null,
                                createdAt = LocalDateTime.now()
                            )

                            val sourceResult = movementRepository.createSource(source)
                            if (sourceResult.isError) {
                                _formState.value = currentState.copy(
                                    isLoading = false,
                                    error = sourceResult.errorOrNull()
                                )
                                return@launch
                            }

                            val expense = Expense(
                                id = UUID.randomUUID().toString(),
                                movementId = movementId,
                                sourceId = sourceId
                            )

                            val expenseResult = movementRepository.createExpense(expense)
                            if (expenseResult.isError) {
                                _formState.value = currentState.copy(
                                    isLoading = false,
                                    error = expenseResult.errorOrNull()
                                )
                                return@launch
                            }
                        }

                        SourceType.RECEIPT -> {
                            val receiptId = UUID.randomUUID().toString()
                            val receiptTotal = currentState.receiptItems.sumOf {
                                val qty = it.quantity.toDouble()
                                val price = it.unitPrice.toDouble()
                                qty * price
                            }

                            val receipt = Receipt(
                                id = receiptId,
                                description = currentState.receiptDescription,
                                datetime = LocalDateTime.now(),
                                currency = currentState.currency,
                                total = receiptTotal,
                                createdAt = LocalDateTime.now()
                            )

                            val receiptResult = receiptRepository.createReceipt(receipt)
                            if (receiptResult.isError) {
                                _formState.value = currentState.copy(
                                    isLoading = false,
                                    error = receiptResult.errorOrNull()
                                )
                                return@launch
                            }

                            for (receiptItemData in currentState.receiptItems) {
                                val itemId = UUID.randomUUID().toString()
                                val item = Item(
                                    id = itemId,
                                    receiptId = receiptId,
                                    description = receiptItemData.name,
                                    quantity = receiptItemData.quantity.toDouble(),
                                    currency = currentState.currency,
                                    unitPrice = receiptItemData.unitPrice.toDouble(),
                                    category = currentState.selectedCategory?.name ?: "",
                                    createdAt = LocalDateTime.now()
                                )

                                val itemResult = itemRepository.createItem(item)
                                if (itemResult.isError) {
                                    _formState.value = currentState.copy(
                                        isLoading = false,
                                        error = itemResult.errorOrNull()
                                    )
                                    return@launch
                                }
                            }

                            val sourceId = UUID.randomUUID().toString()
                            val source = Source(
                                id = sourceId,
                                sourceType = SourceType.RECEIPT,
                                sourceItemId = null,
                                sourceReceiptId = receiptId,
                                createdAt = LocalDateTime.now()
                            )

                            val sourceResult = movementRepository.createSource(source)
                            if (sourceResult.isError) {
                                _formState.value = currentState.copy(
                                    isLoading = false,
                                    error = sourceResult.errorOrNull()
                                )
                                return@launch
                            }

                            val expense = Expense(
                                id = UUID.randomUUID().toString(),
                                movementId = movementId,
                                sourceId = sourceId
                            )

                            val expenseResult = movementRepository.createExpense(expense)
                            if (expenseResult.isError) {
                                _formState.value = currentState.copy(
                                    isLoading = false,
                                    error = expenseResult.errorOrNull()
                                )
                                return@launch
                            }

                            // Process expense entry for shopping list matching
                            // If shoppingItemId is set, update that specific item
                            if (currentState.shoppingItemId != null) {
                                shoppingListRepository.markItemAsBought(
                                    itemId = currentState.shoppingItemId!!,
                                    movementId = movementId,
                                    realPrice = amount
                                )
                            } else {
                                // Otherwise, use fuzzy matching
                                processExpenseMatching(userId, movement)
                            }
                        }
                    }
                }

                MovementType.SAVING -> {
                    val goalId = currentState.selectedGoalId
                    if (goalId.isNullOrBlank()) {
                        _formState.value = currentState.copy(
                            isLoading = false,
                            error = MovementError.INVALID_TYPE
                        )
                        return@launch
                    }

                    // Validar que el monto de saving no exceda el balance disponible
                    val balanceResult = movementRepository.getBalanceByUser(userId)
                    if (balanceResult.isError) {
                        _formState.value = currentState.copy(
                            isLoading = false,
                            error = balanceResult.errorOrNull()
                        )
                        return@launch
                    }

                    val availableBalance = (balanceResult as RepositoryResult.Success).data
                    if (amount > availableBalance) {
                        _formState.value = currentState.copy(
                            isLoading = false,
                            isAmountValid = false,
                            error = MovementError.INSUFFICIENT_BALANCE
                        )
                        return@launch
                    }

                    val saveId = UUID.randomUUID().toString()
                    val save = com.grupo03.solea.data.models.Save(
                        id = saveId,
                        goalId = goalId,
                        amount = amount
                    )

                    val saveResult = movementRepository.createSaving(movement, save)
                    if (saveResult.isError) {
                        _formState.value = currentState.copy(
                            isLoading = false,
                            error = saveResult.errorOrNull()
                        )
                        return@launch
                    }
                }

            }

            // Process expense entry matching with shopping list (if expense was created)
            if (currentState.movementType == MovementType.EXPENSE) {
                processExpenseMatching(userId, movement)
            }

            _formState.value = NewMovementFormState(
                successMessage = "Movimiento creado exitosamente"
            )
            onSuccess()
        }
    }

    /**
     * Processes expense entry and matches it with shopping list items.
     * 
     * Logic:
     * 1. Get active shopping list
     * 2. Iterate over unbought items
     * 3. Fuzzy matching: Compare expense name with item name
     * 4. If match: Mark item as bought and link to movement
     */
    private suspend fun processExpenseMatching(userId: String, movement: Movement) {
        try {
            // Get active shopping list
            val activeListResult = shoppingListRepository.getActiveShoppingList(userId)
            if (activeListResult.isError || 
                activeListResult is RepositoryResult.Success && activeListResult.data == null) {
                return // No active list, nothing to match
            }

            val activeList = (activeListResult as RepositoryResult.Success).data ?: return
            val expenseName = movement.name.lowercase().trim()
            if (expenseName.isEmpty()) return

            // Get real price from movement total
            val realPrice = movement.total

            // Fuzzy matching with unbought items
            val unboughtItems = activeList.items.filter { !it.isBought }
            var bestMatch: com.grupo03.solea.data.models.ShoppingItem? = null
            var bestScore = 0.0

            for (item in unboughtItems) {
                val itemName = item.name.lowercase().trim()
                val score = calculateMatchScore(expenseName, itemName)
                if (score > bestScore && score >= MATCH_THRESHOLD) {
                    bestScore = score
                    bestMatch = item
                }
            }

            // If match found, mark as bought
            if (bestMatch != null) {
                shoppingListRepository.markItemAsBought(
                    itemId = bestMatch.id,
                    movementId = movement.id,
                    realPrice = realPrice
                )
            }
        } catch (e: Exception) {
            // Silently fail - matching is optional
        }
    }

    /**
     * Simple fuzzy matching algorithm using contains and word matching.
     * Returns a score between 0.0 and 1.0.
     */
    private fun calculateMatchScore(expenseName: String, itemName: String): Double {
        val normalizedExpense = normalizeString(expenseName)
        val normalizedItem = normalizeString(itemName)

        // Exact match
        if (normalizedExpense == normalizedItem) {
            return 1.0
        }

        // Contains match
        if (normalizedExpense.contains(normalizedItem) || normalizedItem.contains(normalizedExpense)) {
            val longer = maxOf(normalizedExpense.length, normalizedItem.length)
            val shorter = minOf(normalizedExpense.length, normalizedItem.length)
            return (shorter.toDouble() / longer) * 0.8
        }

        // Word-by-word matching
        val expenseWords = normalizedExpense.split(" ").filter { it.length > 2 }
        val itemWords = normalizedItem.split(" ").filter { it.length > 2 }
        if (expenseWords.isNotEmpty() && itemWords.isNotEmpty()) {
            val matchingWords = expenseWords.count { expenseWord ->
                itemWords.any { itemWord ->
                    expenseWord.contains(itemWord) || itemWord.contains(expenseWord)
                }
            }
            if (matchingWords > 0) {
                return (matchingWords.toDouble() / maxOf(expenseWords.size, itemWords.size)) * 0.6
            }
        }

        return 0.0
    }

    private fun normalizeString(str: String): String {
        return str.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .trim()
    }

    companion object {
        private const val MATCH_THRESHOLD = 0.5
    }

    fun clearForm() {
        _formState.value = NewMovementFormState()
    }

    fun loadFromScannedReceipt(
        establishmentName: String,
        total: String,
        currency: String,
        items: List<com.grupo03.solea.data.models.EditableScannedItem>
    ) {
        val receiptItems = items.map { item ->
            ReceiptItemData(
                name = item.description,
                quantity = item.quantity,
                unitPrice = item.unitPrice
            )
        }

        // Don't override user's currency - keep the one from init
        _formState.value = _formState.value.copy(
            name = establishmentName,
            amount = total,
            movementType = MovementType.EXPENSE,
            sourceType = SourceType.RECEIPT,
            receiptDescription = establishmentName,
            receiptItems = receiptItems
        )
    }
}
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
import com.grupo03.solea.presentation.states.screens.NewMovementFormState
import com.grupo03.solea.presentation.states.screens.ReceiptItemData
import com.grupo03.solea.utils.CurrencyUtils
import com.grupo03.solea.utils.MovementError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

/**
 * ViewModel for the new movement creation form.
 *
 * Handles the complex workflow of creating financial movements (incomes or expenses) with
 * various source types. For expenses, supports two source types:
 * - ITEM: Single item purchase (item with quantity and unit price)
 * - RECEIPT: Multi-item receipt from establishment (receipt with multiple items)
 *
 * The movement creation process involves creating multiple related entities:
 * Movement → Income (for incomes) or Expense → Source → Item/Receipt → Items (for receipts)
 *
 * @property movementRepository Repository for movement, income, expense, and source operations
 * @property categoryRepository Repository for category operations
 * @property itemRepository Repository for item operations
 * @property receiptRepository Repository for receipt operations
 */
class NewMovementFormViewModel(
    private val movementRepository: MovementRepository,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: ItemRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    /** Form state including all fields, validation, and operation status */
    private val _formState = MutableStateFlow(
        NewMovementFormState(currency = CurrencyUtils.getCurrencyByCountry())
    )
    val formState: StateFlow<NewMovementFormState> = _formState.asStateFlow()

    /**
     * Fetches available categories (user + default).
     *
     * @param userId The ID of the user
     */
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

    /** Handles movement name field changes with validation. */
    fun onNameChange(newName: String) {
        val isValid = newName.isNotBlank()
        _formState.value = _formState.value.copy(
            name = newName,
            isNameValid = isValid,
            error = if (!isValid && newName.isNotEmpty()) MovementError.INVALID_TYPE else null
        )
    }

    /** Handles movement description field changes. */
    fun onDescriptionChange(newDescription: String) {
        _formState.value = _formState.value.copy(description = newDescription)
    }

    /** Handles movement amount field changes with validation. */
    fun onAmountChange(newAmount: String) {
        val amount = newAmount.toDoubleOrNull()
        val isValid = amount != null && amount > 0
        _formState.value = _formState.value.copy(
            amount = newAmount,
            isAmountValid = isValid,
            error = if (!isValid && newAmount.isNotEmpty()) MovementError.INVALID_AMOUNT else null
        )
    }

    /** Handles category selection. */
    fun onCategorySelected(category: Category) {
        _formState.value = _formState.value.copy(
            selectedCategory = category,
            isCategorySelected = true,
            error = null
        )
    }

    /** Handles movement type changes (INCOME/EXPENSE). */
    fun onMovementTypeChange(newType: MovementType) {
        _formState.value = _formState.value.copy(movementType = newType)
    }

    /** Handles source type changes for expenses (ITEM/RECEIPT). */
    fun onSourceTypeChange(newSourceType: SourceType) {
        _formState.value = _formState.value.copy(sourceType = newSourceType)
    }

    /** Handles item name field changes (for ITEM source type). */
    fun onItemNameChange(newItemName: String) {
        _formState.value = _formState.value.copy(itemName = newItemName)
    }

    /** Handles item quantity field changes (for ITEM source type). */
    fun onItemQuantityChange(newQuantity: String) {
        _formState.value = _formState.value.copy(itemQuantity = newQuantity)
    }

    /** Handles item unit price field changes (for ITEM source type). */
    fun onItemUnitPriceChange(newUnitPrice: String) {
        _formState.value = _formState.value.copy(itemUnitPrice = newUnitPrice)
    }

    /** Handles receipt description field changes (for RECEIPT source type). */
    fun onReceiptDescriptionChange(newDescription: String) {
        _formState.value = _formState.value.copy(receiptDescription = newDescription)
    }

    /** Adds a new empty item to the receipt items list. */
    fun addReceiptItem() {
        val newItem = ReceiptItemData()
        _formState.value = _formState.value.copy(
            receiptItems = _formState.value.receiptItems + newItem
        )
    }

    /** Removes an item from the receipt items list by index. */
    fun removeReceiptItem(index: Int) {
        val updatedItems = _formState.value.receiptItems.toMutableList()
        updatedItems.removeAt(index)
        _formState.value = _formState.value.copy(receiptItems = updatedItems)
    }

    /** Updates a specific receipt item by index. */
    fun updateReceiptItem(index: Int, item: ReceiptItemData) {
        val updatedItems = _formState.value.receiptItems.toMutableList()
        updatedItems[index] = item
        _formState.value = _formState.value.copy(receiptItems = updatedItems)
    }

    /**
     * Creates a new financial movement with all related entities.
     *
     * This is the main creation method that orchestrates the entire workflow:
     * 1. Validates all form fields
     * 2. Creates the Movement entity
     * 3. For INCOME: Creates Income entity
     * 4. For EXPENSE with ITEM: Creates Item → Source → Expense
     * 5. For EXPENSE with RECEIPT: Creates Receipt → Items → Source → Expense
     *
     * All operations are performed in sequence; if any fails, the process stops
     * and an error is set in the form state.
     *
     * @param userId The ID of the user creating the movement
     * @param onSuccess Callback invoked when all entities are created successfully
     */
    fun createMovement(userId: String, onSuccess: () -> Unit) {
        val currentState = _formState.value

        // Validate name
        if (currentState.name.isBlank()) {
            _formState.value = currentState.copy(
                isNameValid = false,
                error = MovementError.INVALID_TYPE
            )
            return
        }

        // Validate amount
        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _formState.value = currentState.copy(
                isAmountValid = false,
                error = MovementError.INVALID_AMOUNT
            )
            return
        }

        // Validate category - only required for expenses
        if (currentState.movementType == MovementType.EXPENSE && currentState.selectedCategory == null) {
            _formState.value = currentState.copy(
                isCategorySelected = false,
                error = MovementError.INVALID_TYPE
            )
            return
        }

        // Validate source fields for expenses
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
                    // Validate all receipt items
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
            val movement = Movement(
                id = movementId,
                userUid = userId,
                type = currentState.movementType,
                name = currentState.name,
                description = currentState.description,
                datetime = LocalDateTime.now(),
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

            // Create Income or Expense with Source
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
                            // Create Item first
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

                            // Create Source
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

                            // Create Expense
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
                            // Create Receipt first
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

                            // Create all receipt items
                            for (receiptItemData in currentState.receiptItems) {
                                val itemId = UUID.randomUUID().toString()
                                val item = Item(
                                    id = itemId,
                                    receiptId = receiptId,
                                    description = receiptItemData.name,
                                    quantity = receiptItemData.quantity.toDouble(),
                                    currency = currentState.currency,
                                    unitPrice = receiptItemData.unitPrice.toDouble(),
                                    // Receipt items use the movement's category, not individual categories
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

                            // Create Source
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

                            // Create Expense
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
                    }
                }
            }

            _formState.value = NewMovementFormState(
                successMessage = "Movimiento creado exitosamente"
            )
            onSuccess()
        }
    }

    /**
     * Clears the form state, resetting all fields to default values.
     */
    fun clearForm() {
        _formState.value = NewMovementFormState()
    }

    /**
     * Loads data from a scanned receipt into the form.
     *
     * Pre-fills the form with data extracted from receipt scanning (AI OCR).
     * Sets the movement type to EXPENSE and source type to RECEIPT automatically.
     *
     * @param establishmentName Name of the establishment from the receipt
     * @param total Total amount from the receipt
     * @param currency Currency code from the receipt
     * @param items List of scanned items with descriptions, quantities, and prices
     */
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

        _formState.value = _formState.value.copy(
            name = establishmentName,
            amount = total,
            currency = currency,
            movementType = MovementType.EXPENSE,
            sourceType = SourceType.RECEIPT,
            receiptDescription = establishmentName,
            receiptItems = receiptItems
        )
    }
}

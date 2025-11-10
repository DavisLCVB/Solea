package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.SourceType
import com.grupo03.solea.utils.AppError
import com.grupo03.solea.utils.CurrencyUtils

/**
 * UI state for the new movement form.
 *
 * Manages the state of creating a new financial movement (income or expense),
 * including all form fields, validation, and source details for expenses.
 *
 * @property name Movement name/title
 * @property description Movement description
 * @property amount Amount as string (for text field binding)
 * @property selectedCategory Selected category for classification
 * @property movementType Type of movement (EXPENSE or INCOME)
 * @property currency Currency code
 * @property categories Available categories for selection
 * @property isNameValid Whether the name is valid
 * @property isAmountValid Whether the amount is valid
 * @property isCategorySelected Whether a category has been selected
 * @property isLoading Whether a save operation is in progress
 * @property error Error that occurred during save, null if no error
 * @property successMessage Success message to display, null if none
 * @property sourceType Type of expense source (ITEM or RECEIPT) - only for expenses
 * @property itemName Name of single item - for ITEM source type
 * @property itemQuantity Quantity of single item - for ITEM source type
 * @property itemUnitPrice Unit price of single item - for ITEM source type
 * @property receiptDescription Receipt establishment name - for RECEIPT source type
 * @property receiptItems List of items in the receipt - for RECEIPT source type
 * @property isSourceValid Whether the source data is complete and valid
 */
data class NewMovementFormState(
    val name: String = "",
    val description: String = "",
    val amount: String = "",
    val selectedCategory: Category? = null,
    val movementType: MovementType = MovementType.EXPENSE,
    val currency: String = CurrencyUtils.getCurrencyByCountry(),
    val categories: List<Category> = emptyList(),
    val isNameValid: Boolean = true,
    val isAmountValid: Boolean = true,
    val isCategorySelected: Boolean = true,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val successMessage: String? = null,
    // Source fields for expenses
    val sourceType: SourceType = SourceType.ITEM,
    // Single item fields
    val itemName: String = "",
    val itemQuantity: String = "",
    val itemUnitPrice: String = "",
    // Receipt fields
    val receiptDescription: String = "",
    val receiptItems: List<ReceiptItemData> = emptyList(),
    val isSourceValid: Boolean = true,
    // Source files for savings
    val selectedGoalId: String? = null
)

/**
 * Data class representing a single item in a receipt.
 *
 * Used for collecting multiple items when creating a receipt-based expense.
 *
 * @property name Item name/description
 * @property quantity Quantity as string (for text field binding)
 * @property unitPrice Unit price as string (for text field binding)
 */
data class ReceiptItemData(
    val name: String = "",
    val quantity: String = "",
    val unitPrice: String = ""
)

package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.data.models.SourceType
import com.grupo03.solea.utils.AppError

data class NewMovementFormState(
    val name: String = "",
    val description: String = "",
    val amount: String = "",
    val selectedCategory: Category? = null,
    val movementType: MovementType = MovementType.EXPENSE,
    val currency: String = "USD",
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
    val isSourceValid: Boolean = true
)

data class ReceiptItemData(
    val name: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val category: String = ""
)

package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.ShoppingItem
import com.grupo03.solea.data.models.ShoppingListDetails
import com.grupo03.solea.data.models.ShoppingListVoiceResponse
import com.grupo03.solea.utils.AppError

data class ShoppingState(
    val activeList: ShoppingListDetails? = null,
    val isLoading: Boolean = false,
    val isProcessingVoice: Boolean = false,
    val isRecording: Boolean = false,
    val recordingDuration: Long = 0L,
    val hasPermission: Boolean = false,
    val analyzedVoiceData: ShoppingListVoiceResponse? = null,
    val showVoicePreviewDialog: Boolean = false,
    val isEditing: Boolean = false,
    val editingList: ShoppingListDetails? = null,
    val editingListName: String = "",
    val editingItems: List<ShoppingItem> = emptyList(),
    val newItemName: String = "",
    val newItemQuantity: String = "1.0",
    val newItemEstimatedPrice: String = "",
    val shoppingListsHistory: List<com.grupo03.solea.data.models.ShoppingList> = emptyList(),
    val viewedListDetails: ShoppingListDetails? = null,
    val error: AppError? = null,
    val errorMessage: String? = null // Mensaje detallado del error (opcional)
)


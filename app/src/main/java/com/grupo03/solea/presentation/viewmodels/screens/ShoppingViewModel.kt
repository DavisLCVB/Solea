package com.grupo03.solea.presentation.viewmodels.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.ShoppingItem
import com.grupo03.solea.data.models.ShoppingList
import com.grupo03.solea.data.models.ShoppingListDetails
import com.grupo03.solea.data.models.ShoppingListStatus
import com.grupo03.solea.data.models.ShoppingListVoiceResponse
import com.grupo03.solea.data.repositories.interfaces.ShoppingListRepository
import com.grupo03.solea.data.services.interfaces.ShoppingListVoiceService
import com.grupo03.solea.presentation.states.screens.ShoppingState
import com.grupo03.solea.utils.AudioRecorderManager
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.util.UUID
import android.util.Log

/**
 * ViewModel for managing shopping lists and items.
 *
 * Handles creating shopping lists from voice notes, observing active lists,
 * and managing shopping items.
 */
class ShoppingViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListVoiceService: ShoppingListVoiceService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingState())
    val uiState: StateFlow<ShoppingState> = _uiState.asStateFlow()

    private var audioRecorder: AudioRecorderManager? = null
    private var recordingTimerJob: Job? = null
    private var currentRecordingFile: File? = null

    /**
     * Initializes the audio recorder with the provided context.
     */
    fun initializeRecorder(context: Context) {
        if (audioRecorder == null) {
            audioRecorder = AudioRecorderManager(context)
        }
    }

    /**
     * Updates the permission status.
     */
    fun setPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
    }

    /**
     * Starts audio recording.
     */
    fun startRecording() {
        if (_uiState.value.isRecording) return

        audioRecorder?.let { recorder ->
            val result = recorder.startRecording()

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isRecording = true,
                        recordingDuration = 0L,
                        analyzedVoiceData = null,
                        error = null,
                        errorMessage = null
                    )
                }
                startRecordingTimer()
            } else {
                _uiState.update {
                    it.copy(error = com.grupo03.solea.utils.ShoppingListError.CREATION_FAILED)
                }
            }
        }
    }

    /**
     * Stops audio recording and triggers analysis.
     */
    fun stopRecordingAndAnalyze(userUid: String) {
        if (!_uiState.value.isRecording) return

        stopRecordingTimer()

        audioRecorder?.let { recorder ->
            val result = recorder.stopRecording()

            if (result.isSuccess) {
                currentRecordingFile = result.getOrNull()
                _uiState.update { it.copy(isRecording = false) }

                currentRecordingFile?.let { file ->
                    analyzeAudio(file, userUid)
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Error al detener la grabación"
                Log.e("ShoppingViewModel", "Error deteniendo grabación: $errorMessage")
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        error = com.grupo03.solea.utils.ShoppingListError.CREATION_FAILED,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }

    /**
     * Cancels the current recording.
     */
    fun cancelRecording() {
        if (!_uiState.value.isRecording) return

        stopRecordingTimer()

        audioRecorder?.let { recorder ->
            recorder.stopRecording()
            recorder.deleteCurrentRecording()
        }

        _uiState.update {
            it.copy(
                isRecording = false,
                recordingDuration = 0L,
                analyzedVoiceData = null,
                error = null,
                errorMessage = null
            )
        }
    }

    /**
     * Analyzes an audio file and shows preview dialog.
     */
    private fun analyzeAudio(audioFile: File, userUid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingVoice = true, error = null, errorMessage = null) }

            try {
                val result = shoppingListVoiceService.analyzeShoppingListAudio(audioFile)
                if (result.isSuccess) {
                    val voiceResponse = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            isProcessingVoice = false,
                            analyzedVoiceData = voiceResponse,
                            showVoicePreviewDialog = true
                        )
                    }
                } else {
                    // Capturar el mensaje real del error
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido al analizar el audio"
                    Log.e("ShoppingViewModel", "Error analizando audio: $errorMessage")
                    _uiState.update {
                        it.copy(
                            isProcessingVoice = false,
                            error = com.grupo03.solea.utils.ShoppingListError.CREATION_FAILED,
                            errorMessage = errorMessage
                        )
                    }
                }
                audioFile.delete()
            } catch (e: Exception) {
                Log.e("ShoppingViewModel", "Excepción al analizar audio: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isProcessingVoice = false,
                        error = com.grupo03.solea.utils.ShoppingListError.CREATION_FAILED,
                        errorMessage = e.message ?: "Error inesperado al procesar el audio"
                    )
                }
            }
        }
    }

    /**
     * Starts a timer that increments the recording duration every second.
     */
    private fun startRecordingTimer() {
        recordingTimerJob = viewModelScope.launch {
            while (_uiState.value.isRecording) {
                delay(1000)
                _uiState.update {
                    it.copy(recordingDuration = it.recordingDuration + 1)
                }
            }
        }
    }

    /**
     * Stops the recording timer.
     */
    private fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
    }

    /**
     * Closes the voice preview dialog.
     */
    fun closeVoicePreviewDialog() {
        _uiState.update {
            it.copy(
                showVoicePreviewDialog = false,
                analyzedVoiceData = null
            )
        }
    }

    /**
     * Observes the active shopping list for a user (real-time updates).
     */
    fun observeActiveList(userUid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            shoppingListRepository.observeActiveShoppingList(userUid).collect { result ->
                _uiState.update {
                    when (result) {
                        is RepositoryResult.Success -> it.copy(
                            activeList = result.data,
                            isLoading = false,
                            error = null,
                            errorMessage = null
                        )
                        is RepositoryResult.Error -> it.copy(
                            error = result.error,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Creates a shopping list from analyzed voice data.
     * 
     * Steps:
     * 1. Maps response to ShoppingList and ShoppingItems
     * 2. Saves in DB (Status: ACTIVE)
     */
    fun createListFromAnalyzedVoice(
        userUid: String,
        listName: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingVoice = true, error = null, errorMessage = null) }

            try {
                val voiceResponse = _uiState.value.analyzedVoiceData
                    ?: run {
                        _uiState.update {
                            it.copy(
                                isProcessingVoice = false,
                                error = com.grupo03.solea.utils.ShoppingListError.CREATION_FAILED
                            )
                        }
                        return@launch
                    }

                // Step 2: Map to ShoppingList and ShoppingItems
                val listId = UUID.randomUUID().toString()
                val finalListName = listName ?: voiceResponse.shoppingList.listName ?: "Lista de compras"

                val shoppingList = ShoppingList(
                    id = listId,
                    userUid = userUid,
                    name = finalListName,
                    status = ShoppingListStatus.ACTIVE,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                val shoppingItems = voiceResponse.shoppingList.items.map { itemData ->
                    ShoppingItem(
                        id = UUID.randomUUID().toString(),
                        listId = listId,
                        name = itemData.name,
                        quantity = itemData.quantity,
                        estimatedPrice = itemData.estimatedPrice,
                        createdAt = LocalDateTime.now()
                    )
                }

                // Step 3: Save in DB
                val createListResult = shoppingListRepository.createShoppingList(shoppingList)
                if (createListResult.isError) {
                    _uiState.update {
                        it.copy(
                            isProcessingVoice = false,
                            error = createListResult.errorOrNull()
                        )
                    }
                    return@launch
                }

                // Create all items
                for (item in shoppingItems) {
                    shoppingListRepository.createShoppingItem(item)
                    // Continue even if some items fail
                }

                _uiState.update {
                    it.copy(
                        isProcessingVoice = false,
                        showVoicePreviewDialog = false,
                        analyzedVoiceData = null
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingVoice = false,
                        error = com.grupo03.solea.utils.ShoppingListError.CREATION_FAILED
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder?.release()
        stopRecordingTimer()
    }

    /**
     * Checks if a shopping list is complete (all items bought).
     */
    fun checkListCompletion(listId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val listResult = shoppingListRepository.getShoppingListById(listId)
            if (listResult.isError) return@launch

            val list = (listResult as RepositoryResult.Success).data ?: return@launch
            val itemsResult = shoppingListRepository.getShoppingItemsByListId(listId)
            if (itemsResult.isError) return@launch

            val items = (itemsResult as RepositoryResult.Success).data
            val allBought = items.isNotEmpty() && items.all { it.isBought }

            if (allBought) {
                onComplete()
            }
        }
    }

    /**
     * Archives a shopping list (changes status to ARCHIVED).
     */
    fun archiveList(listId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val archiveResult = shoppingListRepository.archiveShoppingList(listId)
            when (archiveResult) {
                is RepositoryResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = archiveResult.error
                        )
                    }
                }
            }
        }
    }

    /**
     * Marks a shopping item as bought manually (from UI checkbox).
     */
    fun markItemAsBought(itemId: String, movementId: String, realPrice: Double?) {
        viewModelScope.launch {
            val result = shoppingListRepository.markItemAsBought(itemId, movementId, realPrice)
            if (result.isError) {
                _uiState.update { it.copy(error = result.errorOrNull()) }
            }
        }
    }

    /**
     * Starts editing mode for the active list.
     */
    fun startEditing() {
        _uiState.value.activeList?.let { list ->
            _uiState.update {
                it.copy(
                    isEditing = true,
                    editingList = list,
                    editingItems = list.items.toList()
                )
            }
        }
    }

    /**
     * Cancels editing mode.
     */
    fun cancelEditing() {
        _uiState.update {
            it.copy(
                isEditing = false,
                editingList = null,
                editingItems = emptyList(),
                newItemName = "",
                newItemQuantity = "1.0",
                newItemEstimatedPrice = ""
            )
        }
    }

    /**
     * Saves the edited list.
     */
    fun saveEditedList(userUid: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val editingList = _uiState.value.editingList ?: return@launch

            try {
                // Update list name if changed
                val updatedList = editingList.shoppingList.copy(
                    name = editingList.shoppingList.name,
                    updatedAt = LocalDateTime.now()
                )
                val updateListResult = shoppingListRepository.updateShoppingList(updatedList)
                if (updateListResult.isError) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = updateListResult.errorOrNull()
                        )
                    }
                    return@launch
                }

                // Get current items from DB
                val currentItemsResult = shoppingListRepository.getShoppingItemsByListId(editingList.shoppingList.id)
                if (currentItemsResult.isError) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = currentItemsResult.errorOrNull()
                        )
                    }
                    return@launch
                }

                val currentItems = (currentItemsResult as RepositoryResult.Success).data
                val editingItems = _uiState.value.editingItems

                // Delete items that were removed (only if not bought)
                val itemsToDelete = currentItems.filter { currentItem ->
                    !editingItems.any { it.id == currentItem.id } && !currentItem.isBought
                }
                for (item in itemsToDelete) {
                    shoppingListRepository.deleteShoppingItem(item.id)
                }

                // Add or update items
                for (item in editingItems) {
                    if (item.id.isEmpty()) {
                        // New item
                        val newItem = item.copy(
                            id = UUID.randomUUID().toString(),
                            listId = editingList.shoppingList.id,
                            createdAt = LocalDateTime.now()
                        )
                        shoppingListRepository.createShoppingItem(newItem)
                    } else {
                        // Update existing item (only if not bought)
                        if (!currentItems.any { it.id == item.id && it.isBought }) {
                            shoppingListRepository.updateShoppingItem(item)
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEditing = false,
                        editingList = null,
                        editingItems = emptyList(),
                        newItemName = "",
                        newItemQuantity = "1.0",
                        newItemEstimatedPrice = ""
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = com.grupo03.solea.utils.ShoppingListError.UPDATE_FAILED
                    )
                }
            }
        }
    }

    /**
     * Adds a new item to the editing list.
     */
    fun addItemToEditingList() {
        val name = _uiState.value.newItemName.trim()
        if (name.isBlank()) return

        val quantity = _uiState.value.newItemQuantity.toDoubleOrNull() ?: 1.0
        val estimatedPrice = _uiState.value.newItemEstimatedPrice.toDoubleOrNull()

        val newItem = ShoppingItem(
            id = "", // Empty ID means new item
            listId = _uiState.value.editingList?.shoppingList?.id ?: "",
            name = name,
            quantity = quantity,
            estimatedPrice = estimatedPrice,
            createdAt = LocalDateTime.now()
        )

        _uiState.update {
            it.copy(
                editingItems = it.editingItems + newItem,
                newItemName = "",
                newItemQuantity = "1.0",
                newItemEstimatedPrice = ""
            )
        }
    }

    /**
     * Removes an item from the editing list (only if not bought).
     */
    fun removeItemFromEditingList(itemId: String) {
        val item = _uiState.value.editingItems.find { it.id == itemId }
        if (item?.isBought == true) return // Cannot remove bought items

        _uiState.update {
            it.copy(editingItems = it.editingItems.filter { it.id != itemId })
        }
    }

    /**
     * Updates new item name.
     */
    fun updateNewItemName(name: String) {
        _uiState.update { it.copy(newItemName = name) }
    }

    /**
     * Updates new item quantity.
     */
    fun updateNewItemQuantity(quantity: String) {
        _uiState.update { it.copy(newItemQuantity = quantity) }
    }

    /**
     * Updates new item estimated price.
     */
    fun updateNewItemEstimatedPrice(price: String) {
        _uiState.update { it.copy(newItemEstimatedPrice = price) }
    }

    /**
     * Cancels a shopping list (changes status to CANCELLED).
     * 
     * A cancelled list remains in history but is no longer active for matching.
     * Can be cancelled even if it has bought items (user decided not to complete it).
     */
    fun cancelList(listId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Update status to CANCELLED
            val listResult = shoppingListRepository.getShoppingListById(listId)
            if (listResult.isError) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = listResult.errorOrNull()
                    )
                }
                onError("Error al obtener la lista")
                return@launch
            }

            val list = (listResult as RepositoryResult.Success).data
            if (list == null) {
                _uiState.update { it.copy(isLoading = false) }
                onError("Lista no encontrada")
                return@launch
            }

            val updatedList = list.copy(
                status = ShoppingListStatus.CANCELLED,
                updatedAt = LocalDateTime.now()
            )

            val updateResult = shoppingListRepository.updateShoppingList(updatedList)
            when (updateResult) {
                is RepositoryResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = updateResult.error
                        )
                    }
                    onError("Error al desestimar la lista")
                }
            }
        }
    }

    /**
     * Deletes a shopping list.
     * Only allowed if no items are bought.
     */
    fun deleteList(listId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Check if any items are bought
            val itemsResult = shoppingListRepository.getShoppingItemsByListId(listId)
            if (itemsResult.isError) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = itemsResult.errorOrNull()
                    )
                }
                onError("Error al verificar items")
                return@launch
            }

            val items = (itemsResult as RepositoryResult.Success).data
            if (items.any { it.isBought }) {
                _uiState.update { it.copy(isLoading = false) }
                onError("No se puede eliminar una lista con items comprados")
                return@launch
            }

            val deleteResult = shoppingListRepository.deleteShoppingList(listId)
            when (deleteResult) {
                is RepositoryResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = deleteResult.error
                        )
                    }
                    onError("Error al eliminar la lista")
                }
            }
        }
    }

    /**
     * Fetches all shopping lists for a user (for history).
     */
    fun fetchShoppingListsHistory(userUid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = shoppingListRepository.getShoppingListsByUserId(userUid, null)
            when (result) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            shoppingListsHistory = result.data
                        )
                    }
                }
                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    /**
     * Fetches shopping list details by ID (for viewing past lists).
     */
    fun fetchShoppingListDetails(listId: String, onSuccess: (ShoppingListDetails) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val listResult = shoppingListRepository.getShoppingListById(listId)
            if (listResult.isError) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = listResult.errorOrNull()
                    )
                }
                return@launch
            }

            val list = (listResult as RepositoryResult.Success).data
            if (list == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = com.grupo03.solea.utils.ShoppingListError.NOT_FOUND
                    )
                }
                return@launch
            }

            val itemsResult = shoppingListRepository.getShoppingItemsByListId(listId)
            val items = if (itemsResult is RepositoryResult.Success) {
                itemsResult.data
            } else {
                emptyList()
            }

            val details = ShoppingListDetails(shoppingList = list, items = items)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    viewedListDetails = details
                )
            }
            onSuccess(details)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, errorMessage = null) }
    }
}


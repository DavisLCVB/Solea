package com.grupo03.solea.presentation.viewmodels.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.EditableScannedItem
import com.grupo03.solea.data.models.EditableScannedReceipt
import com.grupo03.solea.data.repositories.interfaces.CategoryRepository
import com.grupo03.solea.data.services.interfaces.ReceiptScannerService
import com.grupo03.solea.presentation.states.screens.ScanReceiptState
import com.grupo03.solea.utils.MovementError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ScanReceiptViewModel(
    private val receiptScannerService: ReceiptScannerService,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScanReceiptState())
    val state: StateFlow<ScanReceiptState> = _state.asStateFlow()

    fun onImageCaptured(uri: Uri) {
        _state.value = _state.value.copy(
            capturedImageUri = uri,
            error = null
        )
    }

    fun scanReceipt(context: Context, imageUri: Uri, userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, error = null)

            try {
                // Fetch categories (default + user categories)
                val categories = mutableListOf<Category>()

                val defaultCategoriesResult = categoryRepository.getDefaultCategories()
                if (defaultCategoriesResult.isSuccess) {
                    categories.addAll(defaultCategoriesResult.getOrNull() ?: emptyList())
                }

                val userCategoriesResult = categoryRepository.getCategoriesByUser(userId)
                if (userCategoriesResult.isSuccess) {
                    categories.addAll(userCategoriesResult.getOrNull() ?: emptyList())
                }

                // Convert Uri to File
                val imageFile = uriToFile(context, imageUri)

                // Call scanner service with categories
                val result = receiptScannerService.scanReceipt(imageFile, categories)

                if (result.isSuccess) {
                    val scannedData = result.getOrNull()!!.receipt

                    // Convert to editable format
                    val editableReceipt = EditableScannedReceipt(
                        establishmentName = scannedData.establishmentName,
                        date = parseDateTime(scannedData.date),
                        total = scannedData.total.toString(),
                        currency = scannedData.currency,
                        items = scannedData.items.map { item ->
                            EditableScannedItem(
                                description = item.description,
                                quantity = item.quantity.toString(),
                                unitPrice = item.unitPrice.toString()
                            )
                        },
                        suggestedCategory = scannedData.suggestedCategory,
                        confidence = scannedData.confidence
                    )

                    _state.value = _state.value.copy(
                        scannedReceipt = editableReceipt,
                        isScanning = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isScanning = false,
                        error = MovementError.UNKNOWN_ERROR
                    )
                }

                // Clean up temp file
                imageFile.delete()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    error = MovementError.UNKNOWN_ERROR
                )
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("receipt", ".jpg", context.cacheDir)

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun parseDateTime(dateString: String?): LocalDateTime? {
        if (dateString.isNullOrBlank()) return null

        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }

    fun clearState() {
        _state.value = ScanReceiptState()
    }
}

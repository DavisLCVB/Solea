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

/**
 * ViewModel for the receipt scanning screen.
 *
 * Manages the AI-powered receipt scanning workflow, from image capture to OCR processing.
 * Uses the Gemini API via ReceiptScannerService to extract receipt data including
 * establishment name, items, prices, and suggested categories.
 *
 * @property receiptScannerService Service for AI-powered receipt OCR
 * @property categoryRepository Repository for fetching categories for AI categorization
 */
class ScanReceiptViewModel(
    private val receiptScannerService: ReceiptScannerService,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    /** Receipt scanning state including captured image, scanning status, and extracted data */
    private val _state = MutableStateFlow(ScanReceiptState())
    val state: StateFlow<ScanReceiptState> = _state.asStateFlow()

    /**
     * Handles image capture completion.
     *
     * @param uri URI of the captured receipt image
     */
    fun onImageCaptured(uri: Uri) {
        _state.value = _state.value.copy(
            capturedImageUri = uri,
            error = null
        )
    }

    /**
     * Scans a receipt image using AI OCR.
     *
     * Fetches available categories, converts the image URI to a file, and sends it to
     * the ReceiptScannerService for AI processing. Converts the scanner result to
     * editable format for user review before saving as a movement.
     *
     * @param context Android context for URI to file conversion
     * @param imageUri URI of the receipt image to scan
     * @param userId User ID for fetching user-specific categories
     */
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

    /**
     * Converts an Android URI to a temporary file.
     *
     * Creates a temporary file in the app's cache directory and copies the URI content to it.
     *
     * @param context Android context
     * @param uri URI to convert
     * @return Temporary file containing the URI content
     */
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

    /**
     * Parses a date-time string to LocalDateTime.
     *
     * @param dateString ISO 8601 date-time string
     * @return Parsed LocalDateTime, or null if parsing fails or string is blank
     */
    private fun parseDateTime(dateString: String?): LocalDateTime? {
        if (dateString.isNullOrBlank()) return null

        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clears the scanning state, resetting all fields to default values.
     */
    fun clearState() {
        _state.value = ScanReceiptState()
    }
}

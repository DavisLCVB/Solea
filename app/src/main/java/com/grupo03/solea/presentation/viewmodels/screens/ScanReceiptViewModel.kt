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
import com.grupo03.solea.utils.CurrencyUtils
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
     * Clears any previous errors and scanned data to ensure a fresh scan state.
     *
     * @param uri URI of the captured receipt image
     */
    fun onImageCaptured(uri: Uri) {
        _state.value = _state.value.copy(
            capturedImageUri = uri,
            scannedReceipt = null,
            error = null,
            isScanning = false
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
            android.util.Log.d("ScanReceiptVM", "=== INICIANDO ESCANEO ===")
            android.util.Log.d("ScanReceiptVM", "URI imagen: $imageUri")
            android.util.Log.d("ScanReceiptVM", "User ID: $userId")
            _state.value = _state.value.copy(isScanning = true, error = null)

            try {
                // Fetch categories (default + user categories)
                val categories = mutableListOf<Category>()

                android.util.Log.d("ScanReceiptVM", "Obteniendo categorías por defecto...")
                val defaultCategoriesResult = categoryRepository.getDefaultCategories()
                if (defaultCategoriesResult.isSuccess) {
                    val defaultCats = defaultCategoriesResult.getOrNull() ?: emptyList()
                    categories.addAll(defaultCats)
                    android.util.Log.d("ScanReceiptVM", "Categorías por defecto obtenidas: ${defaultCats.size}")
                } else {
                    android.util.Log.w("ScanReceiptVM", "Error obteniendo categorías por defecto")
                }

                android.util.Log.d("ScanReceiptVM", "Obteniendo categorías de usuario...")
                val userCategoriesResult = categoryRepository.getCategoriesByUser(userId)
                if (userCategoriesResult.isSuccess) {
                    val userCats = userCategoriesResult.getOrNull() ?: emptyList()
                    categories.addAll(userCats)
                    android.util.Log.d("ScanReceiptVM", "Categorías de usuario obtenidas: ${userCats.size}")
                } else {
                    android.util.Log.w("ScanReceiptVM", "Error obteniendo categorías de usuario")
                }

                // Convert Uri to File
                android.util.Log.d("ScanReceiptVM", "Convirtiendo URI a archivo...")
                val imageFile = uriToFile(context, imageUri)
                android.util.Log.d("ScanReceiptVM", "Archivo creado: ${imageFile.absolutePath}, tamaño: ${imageFile.length()} bytes")

                // Get device currency
                val deviceCurrency = CurrencyUtils.getCurrencyByCountry()
                android.util.Log.d("ScanReceiptVM", "Moneda del dispositivo: $deviceCurrency")
                android.util.Log.d("ScanReceiptVM", "Total de categorías a enviar: ${categories.size}")

                // Call scanner service with categories and device currency
                android.util.Log.d("ScanReceiptVM", "Llamando al servicio de escaneo...")
                val result = receiptScannerService.scanReceipt(imageFile, categories, deviceCurrency)
                android.util.Log.d("ScanReceiptVM", "Servicio de escaneo completado. Éxito: ${result.isSuccess}")

                if (result.isSuccess) {
                    val scannedData = result.getOrNull()!!.receipt
                    android.util.Log.d("ScanReceiptVM", "Recibo escaneado exitosamente:")
                    android.util.Log.d("ScanReceiptVM", "  - Establecimiento: ${scannedData.establishmentName}")
                    android.util.Log.d("ScanReceiptVM", "  - Fecha: ${scannedData.date}")
                    android.util.Log.d("ScanReceiptVM", "  - Total: ${scannedData.total} ${scannedData.currency}")
                    android.util.Log.d("ScanReceiptVM", "  - Items: ${scannedData.items.size}")
                    android.util.Log.d("ScanReceiptVM", "  - Categoría sugerida: ${scannedData.suggestedCategory}")
                    android.util.Log.d("ScanReceiptVM", "  - Confianza: ${scannedData.confidence}")

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

                    android.util.Log.d("ScanReceiptVM", "Estado actualizado con recibo escaneado")
                    _state.value = _state.value.copy(
                        scannedReceipt = editableReceipt,
                        isScanning = false
                    )
                } else {
                    val error = result.exceptionOrNull()
                    android.util.Log.e("ScanReceiptVM", "Error en el escaneo: ${error?.message}", error)
                    _state.value = _state.value.copy(
                        isScanning = false,
                        error = MovementError.UNKNOWN_ERROR
                    )
                }

                // Clean up temp file
                android.util.Log.d("ScanReceiptVM", "Limpiando archivo temporal...")
                val deleted = imageFile.delete()
                android.util.Log.d("ScanReceiptVM", "Archivo temporal eliminado: $deleted")
            } catch (e: Exception) {
                android.util.Log.e("ScanReceiptVM", "EXCEPCIÓN CAPTURADA en scanReceipt: ${e.message}", e)
                android.util.Log.e("ScanReceiptVM", "Tipo de excepción: ${e.javaClass.name}")
                e.printStackTrace()
                _state.value = _state.value.copy(
                    isScanning = false,
                    error = MovementError.UNKNOWN_ERROR
                )
            }
            android.util.Log.d("ScanReceiptVM", "=== FIN ESCANEO ===")
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
        android.util.Log.d("ScanReceiptVM", "Convirtiendo URI a File: $uri")
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            android.util.Log.e("ScanReceiptVM", "No se pudo abrir InputStream para URI: $uri")
            throw IllegalStateException("No se pudo abrir el archivo de imagen")
        }
        
        val tempFile = File.createTempFile("receipt", ".jpg", context.cacheDir)
        android.util.Log.d("ScanReceiptVM", "Archivo temporal creado: ${tempFile.absolutePath}")

        inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                val bytesCopied = input.copyTo(output)
                android.util.Log.d("ScanReceiptVM", "Bytes copiados: $bytesCopied")
            }
        }

        android.util.Log.d("ScanReceiptVM", "Conversión completada. Tamaño final: ${tempFile.length()} bytes")
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

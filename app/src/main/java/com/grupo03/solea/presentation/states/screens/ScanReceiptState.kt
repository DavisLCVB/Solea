package com.grupo03.solea.presentation.states.screens

import android.net.Uri
import com.grupo03.solea.data.models.EditableScannedReceipt
import com.grupo03.solea.utils.AppError

/**
 * UI state for the receipt scanning screen.
 *
 * Manages the state of the receipt scanning workflow, from capturing
 * the image to displaying the AI-extracted data for editing.
 *
 * @property capturedImageUri URI of the captured receipt image, null if no image captured
 * @property isScanning Whether the AI scanning/OCR process is in progress
 * @property scannedReceipt AI-extracted receipt data ready for user review/editing, null if not scanned yet
 * @property error Error that occurred during scanning, null if no error
 */
data class ScanReceiptState(
    val capturedImageUri: Uri? = null,
    val isScanning: Boolean = false,
    val scannedReceipt: EditableScannedReceipt? = null,
    val error: AppError? = null
)

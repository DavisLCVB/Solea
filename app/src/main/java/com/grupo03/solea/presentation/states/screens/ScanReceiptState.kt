package com.grupo03.solea.presentation.states.screens

import android.net.Uri
import com.grupo03.solea.data.models.EditableScannedReceipt
import com.grupo03.solea.utils.AppError

data class ScanReceiptState(
    val capturedImageUri: Uri? = null,
    val isScanning: Boolean = false,
    val scannedReceipt: EditableScannedReceipt? = null,
    val error: AppError? = null
)

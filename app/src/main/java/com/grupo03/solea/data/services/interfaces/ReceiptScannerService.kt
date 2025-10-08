package com.grupo03.solea.data.services.interfaces

import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.ScannedReceiptResponse
import java.io.File

/**
 * Service interface for scanning receipts using AI
 */
interface ReceiptScannerService {
    /**
     * Scans a receipt image and returns extracted data
     * @param imageFile The receipt image file
     * @param categories List of available categories (default + user categories) to help AI classify items
     * @return Result containing ScannedReceiptResponse or error
     */
    suspend fun scanReceipt(imageFile: File, categories: List<Category> = emptyList()): Result<ScannedReceiptResponse>
}

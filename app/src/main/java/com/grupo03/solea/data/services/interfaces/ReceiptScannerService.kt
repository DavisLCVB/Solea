package com.grupo03.solea.data.services.interfaces

import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.ScannedReceiptResponse
import java.io.File

/**
 * Service interface for scanning and extracting data from receipt images using AI.
 *
 * This service communicates with an external AI-powered OCR service to extract
 * structured data from receipt images, including items, prices, and suggested categories.
 */
interface ReceiptScannerService {

    /**
     * Scans a receipt image and extracts structured data using AI.
     *
     * The AI service analyzes the receipt image and extracts:
     * - Establishment/store name
     * - Purchase date
     * - Individual items with quantities and prices
     * - Total amount
     * - Suggested category based on the items and establishment
     *
     * @param imageFile The receipt image file to scan (supported formats: JPG, PNG)
     * @param categories List of available categories (both default and user-created)
     *                   to help the AI suggest the most appropriate category
     * @param defaultCurrency Default currency code for the device locale (e.g., "PEN", "USD")
     * @return Result containing ScannedReceiptResponse with extracted data on success,
     *         or an error if scanning fails
     */
    suspend fun scanReceipt(
        imageFile: File,
        categories: List<Category> = emptyList(),
        defaultCurrency: String = "USD"
    ): Result<ScannedReceiptResponse>
}

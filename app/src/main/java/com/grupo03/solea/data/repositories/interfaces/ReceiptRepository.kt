package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Receipt
import com.grupo03.solea.utils.RepositoryResult
import java.time.LocalDateTime

/**
 * Repository interface for managing receipts.
 *
 * Receipts contain information about purchases and can be created
 * through camera scanning or manual entry.
 */
interface ReceiptRepository {

    /**
     * Creates a new receipt.
     *
     * @param receipt The receipt to create
     * @return Result containing the created receipt or an error
     */
    suspend fun createReceipt(receipt: Receipt): RepositoryResult<Receipt>

    /**
     * Retrieves a receipt by its ID.
     *
     * @param id Unique identifier of the receipt
     * @return Result containing the receipt if found (nullable) or an error
     */
    suspend fun getReceiptById(id: String): RepositoryResult<Receipt?>

    /**
     * Retrieves multiple receipts by their IDs.
     *
     * @param ids List of receipt identifiers
     * @return Result containing list of found receipts or an error
     */
    suspend fun getReceiptsByIds(ids: List<String>): RepositoryResult<List<Receipt>>

    /**
     * Retrieves receipts within a specific date range.
     *
     * @param startDate Start of the date range (inclusive)
     * @param endDate End of the date range (inclusive)
     * @return Result containing list of receipts in the date range or an error
     */
    suspend fun getReceiptsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): RepositoryResult<List<Receipt>>

    /**
     * Updates an existing receipt.
     *
     * @param receipt The receipt with updated data
     * @return Result containing the updated receipt or an error
     */
    suspend fun updateReceipt(receipt: Receipt): RepositoryResult<Receipt>

    /**
     * Deletes a receipt by its ID.
     *
     * @param id Unique identifier of the receipt to delete
     * @return Result indicating success or error
     */
    suspend fun deleteReceipt(id: String): RepositoryResult<Unit>
}

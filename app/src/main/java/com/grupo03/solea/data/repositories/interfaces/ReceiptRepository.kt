package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Receipt
import com.grupo03.solea.utils.RepositoryResult
import java.time.LocalDateTime

interface ReceiptRepository {

    suspend fun createReceipt(receipt: Receipt): RepositoryResult<Receipt>
    suspend fun getReceiptById(id: String): RepositoryResult<Receipt?>
    suspend fun getReceiptsByIds(ids: List<String>): RepositoryResult<List<Receipt>>
    suspend fun getReceiptsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): RepositoryResult<List<Receipt>>

    suspend fun updateReceipt(receipt: Receipt): RepositoryResult<Receipt>
    suspend fun deleteReceipt(id: String): RepositoryResult<Unit>
}

package com.grupo03.solea.data.repositories.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.grupo03.solea.data.models.Receipt
import com.grupo03.solea.data.repositories.interfaces.ReceiptRepository
import com.grupo03.solea.utils.ReceiptError
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.mapFirestoreException
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset

class FirebaseReceiptRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReceiptRepository {

    private val receiptsCollection = firestore.collection("receipts")

    override suspend fun createReceipt(receipt: Receipt): RepositoryResult<Receipt> {
        return try {
            val receiptData = receipt.toMap()
            receiptsCollection.document(receipt.id).set(receiptData).await()
            RepositoryResult.Success(receipt)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ReceiptError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ReceiptError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getReceiptById(id: String): RepositoryResult<Receipt?> {
        return try {
            val document = receiptsCollection.document(id).get().await()
            val receipt = document.toReceipt()
            RepositoryResult.Success(receipt)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ReceiptError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ReceiptError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getReceiptsByIds(ids: List<String>): RepositoryResult<List<Receipt>> {
        return try {
            if (ids.isEmpty()) {
                return RepositoryResult.Success(emptyList())
            }

            // Firestore has a limit of 10 items for 'in' queries
            // We need to batch the requests if we have more than 10 IDs
            val receipts = mutableListOf<Receipt>()

            ids.chunked(10).forEach { chunk ->
                val documents = receiptsCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()

                receipts.addAll(documents.mapNotNull { it.toReceipt() })
            }

            RepositoryResult.Success(receipts)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ReceiptError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ReceiptError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getReceiptsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): RepositoryResult<List<Receipt>> {
        return try {
            val startTimestamp = startDate.toEpochSecond(ZoneOffset.UTC)
            val endTimestamp = endDate.toEpochSecond(ZoneOffset.UTC)

            val documents = receiptsCollection
                .whereGreaterThanOrEqualTo("datetimeTimestamp", startTimestamp)
                .whereLessThanOrEqualTo("datetimeTimestamp", endTimestamp)
                .get()
                .await()

            val receipts = documents.mapNotNull { it.toReceipt() }
            RepositoryResult.Success(receipts)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ReceiptError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ReceiptError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateReceipt(receipt: Receipt): RepositoryResult<Receipt> {
        return try {
            val receiptData = receipt.toMap()
            receiptsCollection.document(receipt.id).set(receiptData).await()
            RepositoryResult.Success(receipt)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ReceiptError.UPDATE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ReceiptError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteReceipt(id: String): RepositoryResult<Unit> {
        return try {
            receiptsCollection.document(id).delete().await()
            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ReceiptError.DELETE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ReceiptError.UNKNOWN_ERROR)
        }
    }

    // Helper extension functions
    private fun Receipt.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "description" to description,
            "datetimeTimestamp" to datetime.toEpochSecond(ZoneOffset.UTC),
            "currency" to currency,
            "total" to total,
            "createdAtTimestamp" to createdAt.toEpochSecond(ZoneOffset.UTC)
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReceipt(): Receipt? {
        return try {
            Receipt(
                id = getString("id") ?: return null,
                description = getString("description") ?: "",
                datetime = LocalDateTime.ofEpochSecond(
                    getLong("datetimeTimestamp") ?: 0,
                    0,
                    ZoneOffset.UTC
                ),
                currency = getString("currency") ?: "ARS",
                total = getDouble("total") ?: 0.0,
                createdAt = LocalDateTime.ofEpochSecond(
                    getLong("createdAtTimestamp") ?: 0,
                    0,
                    ZoneOffset.UTC
                )
            )
        } catch (_: Exception) {
            null
        }
    }
}

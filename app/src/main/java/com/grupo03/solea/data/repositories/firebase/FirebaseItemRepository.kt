package com.grupo03.solea.data.repositories.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.grupo03.solea.data.models.Item
import com.grupo03.solea.data.repositories.interfaces.ItemRepository
import com.grupo03.solea.utils.ItemError
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.mapFirestoreException
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset

class FirebaseItemRepository(
    firestore: FirebaseFirestore
) : ItemRepository {

    private val itemsCollection = firestore.collection("items")

    override suspend fun createItem(item: Item): RepositoryResult<Item> {
        return try {
            val itemData = item.toMap()
            itemsCollection.document(item.id).set(itemData).await()
            RepositoryResult.Success(item)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ItemError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ItemError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getItemById(id: String): RepositoryResult<Item?> {
        return try {
            val document = itemsCollection.document(id).get().await()
            val item = document.toItem()
            RepositoryResult.Success(item)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ItemError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ItemError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getItemsByIds(ids: List<String>): RepositoryResult<List<Item>> {
        return try {
            if (ids.isEmpty()) {
                return RepositoryResult.Success(emptyList())
            }

            // Firestore has a limit of 10 items for 'in' queries
            // We need to batch the requests if we have more than 10 IDs
            val items = mutableListOf<Item>()

            ids.chunked(10).forEach { chunk ->
                val documents = itemsCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()

                items.addAll(documents.mapNotNull { it.toItem() })
            }

            RepositoryResult.Success(items)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ItemError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ItemError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getItemsByReceiptId(receiptId: String): RepositoryResult<List<Item>> {
        return try {
            val documents = itemsCollection
                .whereEqualTo("receiptId", receiptId)
                .get()
                .await()

            val items = documents.mapNotNull { it.toItem() }
            RepositoryResult.Success(items)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ItemError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ItemError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getItemsByCategory(category: String): RepositoryResult<List<Item>> {
        return try {
            val documents = itemsCollection
                .whereEqualTo("category", category)
                .get()
                .await()

            val items = documents.mapNotNull { it.toItem() }
            RepositoryResult.Success(items)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ItemError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ItemError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateItem(item: Item): RepositoryResult<Item> {
        return try {
            val itemData = item.toMap()
            itemsCollection.document(item.id).set(itemData).await()
            RepositoryResult.Success(item)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ItemError.UPDATE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ItemError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteItem(id: String): RepositoryResult<Unit> {
        return try {
            itemsCollection.document(id).delete().await()
            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ItemError.DELETE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ItemError.UNKNOWN_ERROR)
        }
    }

    // Helper extension functions
    private fun Item.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "receiptId" to receiptId,
            "description" to description,
            "quantity" to quantity,
            "currency" to currency,
            "unitPrice" to unitPrice,
            "totalPrice" to totalPrice,
            "category" to category,
            "createdAtTimestamp" to createdAt.toEpochSecond(ZoneOffset.UTC),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toItem(): Item? {
        return try {
            Item(
                id = getString("id") ?: return null,
                receiptId = getString("receiptId") ?: "",
                description = getString("description") ?: "",
                quantity = getDouble("quantity") ?: 0.0,
                currency = getString("currency") ?: "ARS",
                unitPrice = getDouble("unitPrice") ?: 0.0,
                totalPrice = getDouble("totalPrice") ?: 0.0,
                category = getString("category") ?: "",
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

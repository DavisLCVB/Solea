package com.grupo03.solea.data.repositories.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.grupo03.solea.data.models.ShoppingItem
import com.grupo03.solea.data.models.ShoppingList
import com.grupo03.solea.data.models.ShoppingListDetails
import com.grupo03.solea.data.models.ShoppingListStatus
import com.grupo03.solea.data.repositories.interfaces.ShoppingListRepository
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.ShoppingListError
import com.grupo03.solea.utils.mapFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class FirebaseShoppingListRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ShoppingListRepository {

    companion object {
        private const val SHOPPING_LISTS_COLLECTION = "shoppingLists"
        private const val SHOPPING_ITEMS_COLLECTION = "shoppingItems"
    }

    private val shoppingListsCollection = firestore.collection(SHOPPING_LISTS_COLLECTION)
    private val shoppingItemsCollection = firestore.collection(SHOPPING_ITEMS_COLLECTION)

    override suspend fun createShoppingList(shoppingList: ShoppingList): RepositoryResult<ShoppingList> {
        return try {
            val listData = shoppingList.toMap() ?: return RepositoryResult.Error(
                ShoppingListError.CREATION_FAILED
            )
            shoppingListsCollection.document(shoppingList.id).set(listData).await()
            RepositoryResult.Success(shoppingList)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getShoppingListById(id: String): RepositoryResult<ShoppingList?> {
        return try {
            val document = shoppingListsCollection.document(id).get().await()
            val list = document.toShoppingList()
            RepositoryResult.Success(list)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getActiveShoppingList(userUid: String): RepositoryResult<ShoppingListDetails?> {
        return try {
            val documents = shoppingListsCollection
                .whereEqualTo("userUid", userUid)
                .whereEqualTo("status", ShoppingListStatus.ACTIVE.name)
                .limit(1)
                .get()
                .await()

            if (documents.isEmpty) {
                return RepositoryResult.Success(null)
            }

            val listDoc = documents.documents.first()
            val list = listDoc.toShoppingList() ?: return RepositoryResult.Success(null)

            val itemsResult = getShoppingItemsByListId(list.id)
            val items = if (itemsResult is RepositoryResult.Success) {
                itemsResult.data
            } else {
                emptyList()
            }

            RepositoryResult.Success(ShoppingListDetails(shoppingList = list, items = items))
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getShoppingListsByUserId(
        userUid: String,
        status: ShoppingListStatus?
    ): RepositoryResult<List<ShoppingList>> {
        return try {
            var query = shoppingListsCollection.whereEqualTo("userUid", userUid)
            if (status != null) {
                query = query.whereEqualTo("status", status.name)
            }
            val documents = query.get().await()
            val lists = documents.documents.mapNotNull { it.toShoppingList() }
            RepositoryResult.Success(lists)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateShoppingList(shoppingList: ShoppingList): RepositoryResult<ShoppingList> {
        return try {
            val listData = shoppingList.toMap() ?: return RepositoryResult.Error(
                ShoppingListError.UPDATE_FAILED
            )
            shoppingListsCollection.document(shoppingList.id).set(listData).await()
            RepositoryResult.Success(shoppingList)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.UPDATE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun archiveShoppingList(id: String): RepositoryResult<Unit> {
        return try {
            val listResult = getShoppingListById(id)
            if (listResult.isError) {
                return listResult as RepositoryResult.Error
            }
            val list = (listResult as RepositoryResult.Success).data ?: return RepositoryResult.Error(
                ShoppingListError.FETCH_FAILED
            )
            val updatedList = list.copy(
                status = ShoppingListStatus.ARCHIVED,
                updatedAt = LocalDateTime.now(),
                archivedAt = LocalDateTime.now()
            )

            val updateResult = updateShoppingList(updatedList)

            if (updateResult is RepositoryResult.Success) {
                RepositoryResult.Success(Unit)
            } else {
                updateResult as RepositoryResult.Error
            }
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }


    override suspend fun deleteShoppingList(id: String): RepositoryResult<Unit> {
        return try {
            // Delete all items first
            val itemsResult = getShoppingItemsByListId(id)
            if (itemsResult is RepositoryResult.Success) {
                itemsResult.data.forEach { item ->
                    deleteShoppingItem(item.id)
                }
            }
            // Then delete the list
            shoppingListsCollection.document(id).delete().await()
            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.DELETE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override fun observeActiveShoppingList(userUid: String): Flow<RepositoryResult<ShoppingListDetails?>> {
        return callbackFlow {
            var itemsListener: com.google.firebase.firestore.ListenerRegistration? = null
            val listListener = shoppingListsCollection
                .whereEqualTo("userUid", userUid)
                .whereEqualTo("status", ShoppingListStatus.ACTIVE.name)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(RepositoryResult.Error(
                            mapFirestoreException(error, ShoppingListError.FETCH_FAILED)
                        ))
                        return@addSnapshotListener
                    }

                    val list = snapshot?.documents?.firstOrNull()?.toShoppingList()
                    if (list != null) {
                        // Remove previous items listener
                        itemsListener?.remove()
                        
                        // Observe items in real-time
                        itemsListener = shoppingItemsCollection
                            .whereEqualTo("listId", list.id)
                            .addSnapshotListener { itemsSnapshot, itemsError ->
                                if (itemsError != null) {
                                    trySend(RepositoryResult.Error(
                                        mapFirestoreException(itemsError, ShoppingListError.FETCH_FAILED)
                                    ))
                                    return@addSnapshotListener
                                }

                                val items = itemsSnapshot?.documents?.mapNotNull { it.toShoppingItem() } ?: emptyList()
                                trySend(RepositoryResult.Success(ShoppingListDetails(shoppingList = list, items = items)))
                            }
                    } else {
                        itemsListener?.remove()
                        trySend(RepositoryResult.Success(null))
                    }
                }

            awaitClose { 
                listListener.remove()
                itemsListener?.remove()
            }
        }
    }

    override fun observeShoppingItemsByListId(listId: String): Flow<RepositoryResult<List<ShoppingItem>>> {
        return callbackFlow {
            val listener = shoppingItemsCollection
                .whereEqualTo("listId", listId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(RepositoryResult.Error(
                            mapFirestoreException(error, ShoppingListError.FETCH_FAILED)
                        ))
                        return@addSnapshotListener
                    }

                    val items = snapshot?.documents?.mapNotNull { it.toShoppingItem() } ?: emptyList()
                    trySend(RepositoryResult.Success(items))
                }

            awaitClose { listener.remove() }
        }
    }

    override suspend fun createShoppingItem(item: ShoppingItem): RepositoryResult<ShoppingItem> {
        return try {
            val itemData = item.toMap() ?: return RepositoryResult.Error(
                ShoppingListError.CREATION_FAILED
            )
            shoppingItemsCollection.document(item.id).set(itemData).await()
            RepositoryResult.Success(item)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.CREATION_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getShoppingItemById(id: String): RepositoryResult<ShoppingItem?> {
        return try {
            val document = shoppingItemsCollection.document(id).get().await()
            val item = document.toShoppingItem()
            RepositoryResult.Success(item)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getShoppingItemsByListId(listId: String): RepositoryResult<List<ShoppingItem>> {
        return try {
            val documents = shoppingItemsCollection
                .whereEqualTo("listId", listId)
                .get()
                .await()
            val items = documents.documents.mapNotNull { it.toShoppingItem() }
            RepositoryResult.Success(items)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.FETCH_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateShoppingItem(item: ShoppingItem): RepositoryResult<ShoppingItem> {
        return try {
            val itemData = item.toMap() ?: return RepositoryResult.Error(
                ShoppingListError.UPDATE_FAILED
            )
            shoppingItemsCollection.document(item.id).set(itemData).await()
            RepositoryResult.Success(item)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.UPDATE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteShoppingItem(id: String): RepositoryResult<Unit> {
        return try {
            shoppingItemsCollection.document(id).delete().await()
            RepositoryResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RepositoryResult.Error(mapFirestoreException(e, ShoppingListError.DELETE_FAILED))
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    override suspend fun markItemAsBought(
        itemId: String,
        movementId: String,
        realPrice: Double?
    ): RepositoryResult<ShoppingItem> {
        return try {
            val itemResult = getShoppingItemById(itemId)
            if (itemResult.isError) {
                return itemResult as RepositoryResult.Error
            }
            val item = (itemResult as RepositoryResult.Success).data
                ?: return RepositoryResult.Error(ShoppingListError.FETCH_FAILED)
            val updatedItem = item.copy(
                isBought = true,
                linkedMovementId = movementId,
                realPrice = realPrice,
                boughtAt = LocalDateTime.now()
            )
            updateShoppingItem(updatedItem)
        } catch (_: Exception) {
            RepositoryResult.Error(ShoppingListError.UNKNOWN_ERROR)
        }
    }

    // Extension functions for Firestore conversion
    private fun com.google.firebase.firestore.DocumentSnapshot.toShoppingList(): ShoppingList? {
        return ShoppingList.fromMap(data ?: emptyMap())
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toShoppingItem(): ShoppingItem? {
        return ShoppingItem.fromMap(data ?: emptyMap())
    }
}

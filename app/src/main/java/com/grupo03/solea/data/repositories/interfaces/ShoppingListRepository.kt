package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.ShoppingItem
import com.grupo03.solea.data.models.ShoppingList
import com.grupo03.solea.data.models.ShoppingListDetails
import com.grupo03.solea.utils.RepositoryResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing shopping lists and items.
 */
interface ShoppingListRepository {
    /**
     * Creates a new shopping list.
     */
    suspend fun createShoppingList(shoppingList: ShoppingList): RepositoryResult<ShoppingList>

    /**
     * Gets a shopping list by ID.
     */
    suspend fun getShoppingListById(id: String): RepositoryResult<ShoppingList?>

    /**
     * Gets the active shopping list for a user.
     */
    suspend fun getActiveShoppingList(userUid: String): RepositoryResult<ShoppingListDetails?>

    /**
     * Gets all shopping lists for a user (optionally filtered by status).
     */
    suspend fun getShoppingListsByUserId(
        userUid: String,
        status: com.grupo03.solea.data.models.ShoppingListStatus? = null
    ): RepositoryResult<List<ShoppingList>>

    /**
     * Updates a shopping list.
     */
    suspend fun updateShoppingList(shoppingList: ShoppingList): RepositoryResult<ShoppingList>

    /**
     * Archives a shopping list (changes status to ARCHIVED).
     */
    suspend fun archiveShoppingList(id: String): RepositoryResult<Unit>

    /**
     * Deletes a shopping list and all its items.
     */
    suspend fun deleteShoppingList(id: String): RepositoryResult<Unit>

    /**
     * Observes the active shopping list for a user (real-time updates).
     */
    fun observeActiveShoppingList(userUid: String): Flow<RepositoryResult<ShoppingListDetails?>>

    /**
     * Observes shopping items for a list (real-time updates).
     */
    fun observeShoppingItemsByListId(listId: String): Flow<RepositoryResult<List<ShoppingItem>>>

    /**
     * Creates a shopping item.
     */
    suspend fun createShoppingItem(item: ShoppingItem): RepositoryResult<ShoppingItem>

    /**
     * Gets a shopping item by ID.
     */
    suspend fun getShoppingItemById(id: String): RepositoryResult<ShoppingItem?>

    /**
     * Gets all items for a shopping list.
     */
    suspend fun getShoppingItemsByListId(listId: String): RepositoryResult<List<ShoppingItem>>

    /**
     * Updates a shopping item.
     */
    suspend fun updateShoppingItem(item: ShoppingItem): RepositoryResult<ShoppingItem>

    /**
     * Deletes a shopping item.
     */
    suspend fun deleteShoppingItem(id: String): RepositoryResult<Unit>

    /**
     * Marks a shopping item as bought and links it to a movement.
     */
    suspend fun markItemAsBought(
        itemId: String,
        movementId: String,
        realPrice: Double?
    ): RepositoryResult<ShoppingItem>
}


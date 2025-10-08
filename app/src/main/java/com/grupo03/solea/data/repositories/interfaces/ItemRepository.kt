package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Item
import com.grupo03.solea.utils.RepositoryResult

/**
 * Repository interface for managing purchase items.
 *
 * Items can exist as standalone purchases or as part of a receipt.
 */
interface ItemRepository {

    /**
     * Creates a new item.
     *
     * @param item The item to create
     * @return Result containing the created item or an error
     */
    suspend fun createItem(item: Item): RepositoryResult<Item>

    /**
     * Retrieves an item by its ID.
     *
     * @param id Unique identifier of the item
     * @return Result containing the item if found (nullable) or an error
     */
    suspend fun getItemById(id: String): RepositoryResult<Item?>

    /**
     * Retrieves multiple items by their IDs.
     *
     * @param ids List of item identifiers
     * @return Result containing list of found items or an error
     */
    suspend fun getItemsByIds(ids: List<String>): RepositoryResult<List<Item>>

    /**
     * Retrieves all items associated with a specific receipt.
     *
     * @param receiptId Unique identifier of the receipt
     * @return Result containing list of items in the receipt or an error
     */
    suspend fun getItemsByReceiptId(receiptId: String): RepositoryResult<List<Item>>

    /**
     * Retrieves items filtered by category.
     *
     * @param category Category name to filter by
     * @return Result containing list of items in the category or an error
     */
    suspend fun getItemsByCategory(category: String): RepositoryResult<List<Item>>

    /**
     * Updates an existing item.
     *
     * @param item The item with updated data
     * @return Result containing the updated item or an error
     */
    suspend fun updateItem(item: Item): RepositoryResult<Item>

    /**
     * Deletes an item by its ID.
     *
     * @param id Unique identifier of the item to delete
     * @return Result indicating success or error
     */
    suspend fun deleteItem(id: String): RepositoryResult<Unit>
}

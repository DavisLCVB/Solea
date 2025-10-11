package com.grupo03.solea.data.repositories.interfaces

import com.grupo03.solea.data.models.Item
import com.grupo03.solea.utils.RepositoryResult

interface ItemRepository {

    suspend fun createItem(item: Item): RepositoryResult<Item>
    suspend fun getItemById(id: String): RepositoryResult<Item?>
    suspend fun getItemsByIds(ids: List<String>): RepositoryResult<List<Item>>
    suspend fun getItemsByReceiptId(receiptId: String): RepositoryResult<List<Item>>
    suspend fun getItemsByCategory(category: String): RepositoryResult<List<Item>>
    suspend fun updateItem(item: Item): RepositoryResult<Item>
    suspend fun deleteItem(id: String): RepositoryResult<Unit>
}

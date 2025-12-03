package com.grupo03.solea.data.models

import com.grupo03.solea.utils.FromMap
import com.grupo03.solea.utils.ToMap
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Represents an item in a shopping list (item premeditado).
 *
 * Unlike the existing Item model (which represents purchased items with prices),
 * ShoppingItem represents an intention to buy something. It doesn't have a
 * final price or receiptId until it's actually purchased.
 *
 * @property id Unique identifier for the shopping item
 * @property listId ID of the ShoppingList this item belongs to
 * @property name Name or description of the item to buy
 * @property quantity Expected quantity to buy (optional, defaults to 1)
 * @property isBought Whether this item has been purchased
 * @property linkedMovementId ID of the Movement (expense) that fulfilled this item (null if not bought)
 * @property estimatedPrice Optional estimated price (for budgeting, not final)
 * @property realPrice Actual price paid when purchased (null until bought)
 * @property createdAt Timestamp when this item was added to the list
 * @property boughtAt Timestamp when this item was marked as bought (null if not bought)
 */
data class ShoppingItem(
    val id: String = "",
    val listId: String = "",
    val name: String = "",
    val quantity: Double = 1.0,
    val isBought: Boolean = false,
    val linkedMovementId: String? = null,
    val estimatedPrice: Double? = null,
    val realPrice: Double? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val boughtAt: LocalDateTime? = null
) : ToMap {
    override fun toMap(): Map<String, Any?>? {
        val map = mutableMapOf<String, Any?>(
            "id" to id,
            "listId" to listId,
            "name" to name,
            "quantity" to quantity,
            "isBought" to isBought,
            "createdAtTimestamp" to createdAt.toEpochSecond(ZoneOffset.UTC)
        )
        linkedMovementId?.let { map["linkedMovementId"] = it }
        estimatedPrice?.let { map["estimatedPrice"] = it }
        realPrice?.let { map["realPrice"] = it }
        boughtAt?.let { map["boughtAtTimestamp"] = it.toEpochSecond(ZoneOffset.UTC) }
        return map
    }

    companion object : FromMap<ShoppingItem> {
        override fun fromMap(map: Map<String, Any?>): ShoppingItem? {
            val id = map["id"] as? String ?: return null
            val listId = map["listId"] as? String ?: return null
            val name = map["name"] as? String ?: ""
            val quantity = (map["quantity"] as? Number)?.toDouble() ?: 1.0
            val isBought = map["isBought"] as? Boolean ?: false
            val linkedMovementId = map["linkedMovementId"] as? String
            val estimatedPrice = (map["estimatedPrice"] as? Number)?.toDouble()
            val realPrice = (map["realPrice"] as? Number)?.toDouble()
            val createdAtTimestamp = (map["createdAtTimestamp"] as? Number)?.toLong() ?: 0L
            val createdAt = LocalDateTime.ofEpochSecond(createdAtTimestamp, 0, ZoneOffset.UTC)
            val boughtAtTimestamp = (map["boughtAtTimestamp"] as? Number)?.toLong()
            val boughtAt = boughtAtTimestamp?.let {
                LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
            }

            return ShoppingItem(
                id = id,
                listId = listId,
                name = name,
                quantity = quantity,
                isBought = isBought,
                linkedMovementId = linkedMovementId?.takeIf { it.isNotEmpty() },
                estimatedPrice = estimatedPrice,
                realPrice = realPrice,
                createdAt = createdAt,
                boughtAt = boughtAt
            )
        }
    }
}

/**
 * Complete shopping list information with all related items.
 *
 * This aggregates a ShoppingList with its ShoppingItems, providing
 * a complete view of the shopping list and its items.
 *
 * @property shoppingList The shopping list record
 * @property items List of items in this shopping list
 */
data class ShoppingListDetails(
    val shoppingList: ShoppingList = ShoppingList(),
    val items: List<ShoppingItem> = emptyList()
)


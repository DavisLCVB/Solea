package com.grupo03.solea.data.models

import com.grupo03.solea.utils.FromMap
import com.grupo03.solea.utils.ToMap
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Status of a shopping list.
 */
enum class ShoppingListStatus {
    ACTIVE,    // List is currently being used
    ARCHIVED,  // List has been completed and archived
    CANCELLED  // List was cancelled
}

/**
 * Represents a shopping list (agrupación de items de compra).
 *
 * A ShoppingList contains multiple ShoppingItems and tracks the overall
 * status and metadata of a shopping session.
 *
 * @property id Unique identifier for the shopping list
 * @property userUid ID of the user who owns this list
 * @property name Name or title of the shopping list
 * @property status Current status of the list (ACTIVE, ARCHIVED, CANCELLED)
 * @property createdAt Timestamp when this list was created
 * @property updatedAt Timestamp when this list was last updated
 * @property archivedAt Timestamp when this list was archived (null if not archived)
 */
data class ShoppingList(
    val id: String = "",
    val userUid: String = "",
    val name: String = "",
    val status: ShoppingListStatus = ShoppingListStatus.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val archivedAt: LocalDateTime? = null
) : ToMap {
    override fun toMap(): Map<String, Any?>? {
        val map = mutableMapOf<String, Any?>(
            "id" to id,
            "userUid" to userUid,
            "name" to name,
            "status" to status.name,
            "createdAtTimestamp" to createdAt.toEpochSecond(ZoneOffset.UTC),
            "updatedAtTimestamp" to updatedAt.toEpochSecond(ZoneOffset.UTC)
        )
        archivedAt?.let {
            map["archivedAtTimestamp"] = it.toEpochSecond(ZoneOffset.UTC)
        }
        return map
    }

    companion object : FromMap<ShoppingList> {
        override fun fromMap(map: Map<String, Any?>): ShoppingList? {
            val id = map["id"] as? String ?: return null
            val userUid = map["userUid"] as? String ?: return null
            val name = map["name"] as? String ?: ""
            val statusString = map["status"] as? String ?: "ACTIVE"
            val status = try {
                ShoppingListStatus.valueOf(statusString)
            } catch (e: Exception) {
                ShoppingListStatus.ACTIVE
            }
            val createdAtTimestamp = (map["createdAtTimestamp"] as? Number)?.toLong() ?: 0L
            val createdAt = LocalDateTime.ofEpochSecond(createdAtTimestamp, 0, ZoneOffset.UTC)
            val updatedAtTimestamp = (map["updatedAtTimestamp"] as? Number)?.toLong() ?: createdAtTimestamp
            val updatedAt = LocalDateTime.ofEpochSecond(updatedAtTimestamp, 0, ZoneOffset.UTC)
            val archivedAtTimestamp = (map["archivedAtTimestamp"] as? Number)?.toLong()
            val archivedAt = archivedAtTimestamp?.let {
                LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
            }

            return ShoppingList(
                id = id,
                userUid = userUid,
                name = name,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt,
                archivedAt = archivedAt
            )
        }
    }
}


package com.grupo03.solea.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import java.time.LocalDateTime

/**
 * Room entity for caching Movement data locally.
 * This serves as a local cache to improve performance and enable offline access.
 */
@Entity(tableName = "movements")
data class MovementEntity(
    @PrimaryKey
    val id: String,
    val userUid: String,
    val type: MovementType,
    val name: String,
    val description: String,
    val datetime: LocalDateTime,
    val currency: String,
    val total: Double,
    val category: String?,
    val createdAt: LocalDateTime,
    val lastSyncedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Converts a Movement domain model to a MovementEntity for Room storage.
 */
fun Movement.toEntity(): MovementEntity {
    return MovementEntity(
        id = id,
        userUid = userUid,
        type = type,
        name = name,
        description = description,
        datetime = datetime,
        currency = currency,
        total = total,
        category = category,
        createdAt = createdAt
    )
}

/**
 * Converts a MovementEntity from Room to a Movement domain model.
 */
fun MovementEntity.toModel(): Movement {
    return Movement(
        id = id,
        userUid = userUid,
        type = type,
        name = name,
        description = description,
        datetime = datetime,
        currency = currency,
        total = total,
        category = category,
        createdAt = createdAt
    )
}

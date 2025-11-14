package com.grupo03.solea.data.models

import java.time.LocalDateTime

/**
 * Type of financial movement.
 */
enum class MovementType {
    EXPENSE,

    INCOME,

    SAVING
}

/**
 * Represents a financial movement (transaction) in the application.
 *
 * A Movement can be either an expense or an income. This is the base entity
 * that tracks all financial transactions for a user.
 *
 * @property id Unique identifier for the movement
 * @property userUid ID of the user who owns this movement
 * @property type Type of movement (EXPENSE or INCOME)
 * @property name Short name or title of the movement
 * @property description Detailed description of the movement
 * @property datetime Date and time when the movement occurred
 * @property currency Currency code (e.g., "USD", "EUR", "ARS")
 * @property total Total amount of the movement
 * @property category Optional category name for classification
 * @property createdAt Timestamp when this record was created
 */
data class Movement(
    val id: String = "",
    val userUid: String = "",
    val type: MovementType = MovementType.EXPENSE,
    val name: String = "",
    val description: String = "",
    val datetime: LocalDateTime = LocalDateTime.now(),
    val currency: String = "USD",
    val total: Double = 0.0,
    val category: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

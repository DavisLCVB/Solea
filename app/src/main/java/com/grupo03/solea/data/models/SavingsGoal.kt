package com.grupo03.solea.data.models

import com.grupo03.solea.utils.FromMap
import com.grupo03.solea.utils.ToMap
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Represents a savings goal for the user.
 *
 * Users can set financial goals with a target amount and deadline.
 * The current saved amount is tracked separately.
 *
 * @property id Unique identifier for the goal
 * @property userId ID of the user who created this goal
 * @property name Name/description of the goal (e.g., "Laptop", "Vacation")
 * @property targetAmount The amount to save
 * @property currentAmount Current saved amount towards the goal
 * @property currency Currency code
 * @property deadline Target date to achieve this goal
 * @property createdAt Timestamp when this goal was created
 * @property isCompleted Whether the goal has been achieved
 */
data class SavingsGoal(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val currency: String = "USD",
    val deadline: Instant = Instant.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isCompleted: Boolean = false
) : ToMap {
    /**
     * Calculates the progress percentage (0-100)
     */
    val progressPercentage: Int
        get() = if (targetAmount > 0) {
            ((currentAmount / targetAmount) * 100).toInt().coerceIn(0, 100)
        } else 0

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "name" to name,
            "targetAmount" to targetAmount,
            "currentAmount" to currentAmount,
            "currency" to currency,
            "deadline" to deadline.toString(),
            "createdAtTimestamp" to createdAt.toEpochSecond(ZoneOffset.UTC),
            "isCompleted" to isCompleted
        )
    }

    companion object : FromMap<SavingsGoal> {
        const val TAG = "SavingsGoalConversion"
        override fun fromMap(map: Map<String, Any?>): SavingsGoal? {
            val id = map["id"] as? String ?: return null
            val userId = map["userId"] as? String ?: return null
            val name = map["name"] as? String ?: return null
            val targetAmount = (map["targetAmount"] as? Number)?.toDouble() ?: return null
            val currentAmount = (map["currentAmount"] as? Number)?.toDouble() ?: 0.0
            val currency = map["currency"] as? String ?: "USD"
            val deadlineString = map["deadline"] as? String ?: return null
            val deadline = try {
                Instant.parse(deadlineString)
            } catch (e: Exception) {
                return null
            }
            val createdAtTimestamp = (map["createdAtTimestamp"] as? Number)?.toLong() ?: return null
            val createdAt = LocalDateTime.ofEpochSecond(createdAtTimestamp, 0, ZoneOffset.UTC)
            val isCompleted = map["isCompleted"] as? Boolean ?: false

            return SavingsGoal(
                id = id,
                userId = userId,
                name = name,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                currency = currency,
                deadline = deadline,
                createdAt = createdAt,
                isCompleted = isCompleted
            )
        }
    }
}
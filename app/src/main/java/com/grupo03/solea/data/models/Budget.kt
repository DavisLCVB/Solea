package com.grupo03.solea.data.models

import com.grupo03.solea.utils.FromMap
import com.grupo03.solea.utils.ToMap
import java.time.Instant

/**
 * Represents a budget limit for a specific category.
 *
 * Budgets allow users to set spending limits on categories for a specific time period.
 * Each budget has a status that indicates if it's active, expired, exceeded, etc.
 *
 * @property id Unique identifier for the budget
 * @property userId ID of the user who created this budget
 * @property category Name of the category this budget applies to
 * @property amount Maximum amount allowed for this budget period
 * @property until Expiration date/time of this budget
 * @property statusId ID of the current status of this budget
 */
data class Budget(
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val until: Instant = Instant.now(),
    val statusId: String = ""
) : ToMap {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "category" to category,
            "amount" to amount,
            "until" to until.toString(),
            "statusId" to statusId
        )
    }

    companion object : FromMap<Budget> {
        const val TAG = "BudgetConversion"
        override fun fromMap(map: Map<String, Any?>): Budget? {
            val id = map["id"] as? String ?: return null
            val userId = map["userId"] as? String ?: return null
            val category = map["category"] as? String ?: return null
            val amount = (map["amount"] as? Number)?.toDouble() ?: return null
            val untilString = map["until"] as? String ?: return null
            val until = try {
                Instant.parse(untilString)
            } catch (e: Exception) {
                return null
            }
            val statusId = map["statusId"] as? String ?: return null

            return Budget(
                id = id,
                userId = userId,
                category = category,
                amount = amount,
                until = until,
                statusId = statusId
            )
        }
    }
}

/**
 * Represents the status of a budget.
 *
 * Status values are predefined in the system and indicate the current state
 * of a budget (e.g., "active", "expired", "exceeded").
 *
 * @property id Unique identifier for the status
 * @property value Human-readable status value
 */
data class Status(
    val id: String = "",
    val value: String = ""
) : ToMap {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "value" to value
        )
    }

    companion object : FromMap<Status> {
        override fun fromMap(map: Map<String, Any?>): Status? {
            val id = map["id"] as? String ?: return null
            val value = map["value"] as? String ?: return null
            return Status(id = id, value = value)
        }
    }
}

package com.grupo03.solea.data.models

import com.grupo03.solea.utils.FromMap
import com.grupo03.solea.utils.ToMap
import java.time.Instant
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
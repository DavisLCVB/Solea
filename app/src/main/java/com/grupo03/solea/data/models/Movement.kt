package com.grupo03.solea.data.models

import android.util.Log
import com.grupo03.solea.utils.FromMap
import com.grupo03.solea.utils.ToMap
import java.time.Instant

data class Movement(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val date: Instant = Instant.now(),
    val item: String = "",
    val note: String = "",
    val typeId: String = "",
) : ToMap {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "amount" to amount,
            "date" to date.toString(),
            "item" to item,
            "note" to note,
            "typeId" to typeId,
        )
    }

    companion object : FromMap<Movement> {
        const val TAG = "Conversion"
        override fun fromMap(map: Map<String, Any?>): Movement? {
            val id = map["id"] as? String ?: return null
            val userId = map["userId"] as? String ?: return null
            val amount = (map["amount"] as? Number)?.toDouble() ?: return null
            val note = map["note"] as? String ?: ""
            val dateString = map["date"] as? String ?: return null
            val item = map["item"] as? String ?: ""
            val date = try {
                Instant.parse(dateString)
            } catch (e: Exception) {
                Log.d(TAG, "fromMap: Error parsing date: $dateString", e)
                return null
            }
            val typeId = map["typeId"] as? String ?: return null

            return Movement(
                id = id,
                userId = userId,
                amount = amount,
                date = date,
                typeId = typeId,
                note = note,
                item = item
            )
        }
    }
}

data class MovementType(
    val id: String = "",
    val userId: String = "",
    val value: String = "",
    val description: String = ""
) : ToMap {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "value" to value,
            "description" to description,
        )
    }

    companion object : FromMap<MovementType> {
        const val TAG = "Conversion"
        override fun fromMap(map: Map<String, Any?>): MovementType? {
            val id = map["id"] as? String ?: return null
            val userId = map["userId"] as? String ?: return null
            val value = map["value"] as? String ?: return null
            val description = map["description"] as? String ?: return null

            return MovementType(
                id = id,
                userId = userId,
                value = value,
                description = description
            )
        }
    }
}

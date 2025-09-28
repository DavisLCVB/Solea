package com.grupo03.solea.data.models

import java.time.Instant

data class Movement(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val date: Instant = Instant.now(),
    val typeId: String = "",
)

data class MovementType(
    val id: String = "",
    val userId: String = "",
    val value: String = "",
    val description: String = ""
)

fun toMap(movement: Movement): Map<String, Any> {
    return mapOf(
        "id" to movement.id,
        "userId" to movement.userId,
        "amount" to movement.amount,
        "date" to movement.date.toString(),
        "typeId" to movement.typeId,
    )
}

fun toMap(type: MovementType): Map<String, Any> {
    return mapOf(
        "id" to type.id,
        "userId" to type.userId,
        "value" to type.value,
        "description" to type.description,
    )
}
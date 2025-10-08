package com.grupo03.solea.data.models

import java.time.LocalDateTime

enum class MovementType {
    EXPENSE,
    INCOME
}

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

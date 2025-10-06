package com.grupo03.solea.data.models

import java.time.LocalDateTime

data class Item(
    val id: String = "",
    val receiptId: String? = null,
    val description: String = "",
    val quantity: Double = 0.0,
    val currency: String = "USD",
    val unitPrice: Double = 0.0,
    val totalPrice: Double = quantity * unitPrice,
    val category: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

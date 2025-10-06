package com.grupo03.solea.data.models

import java.time.LocalDateTime

data class Receipt(
    val id: String = "",
    val description: String = "",
    val datetime: LocalDateTime = LocalDateTime.now(),
    val currency: String = "USD",
    val total: Double = 0.0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
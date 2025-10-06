package com.grupo03.solea.data.models

import java.time.LocalDateTime

enum class SourceType {
    ITEM,
    RECEIPT
}

data class Source(
    val id: String = "",
    val sourceType: SourceType = SourceType.RECEIPT,
    val sourceItemId: String? = null,
    val sourceReceiptId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
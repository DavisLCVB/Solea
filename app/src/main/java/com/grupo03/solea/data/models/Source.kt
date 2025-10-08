package com.grupo03.solea.data.models

import java.time.LocalDateTime

/**
 * Type of expense source.
 */
enum class SourceType {
    /** Single item purchase */
    ITEM,

    /** Receipt containing multiple items */
    RECEIPT
}

/**
 * Represents the source/origin of an expense.
 *
 * A Source acts as a discriminator that points to either a single Item or a Receipt.
 * Only one of sourceItemId or sourceReceiptId should be set, depending on the sourceType.
 *
 * @property id Unique identifier for the source
 * @property sourceType Type of source (ITEM or RECEIPT)
 * @property sourceItemId ID of the associated Item (set when sourceType is ITEM)
 * @property sourceReceiptId ID of the associated Receipt (set when sourceType is RECEIPT)
 * @property createdAt Timestamp when this source record was created
 */
data class Source(
    val id: String = "",
    val sourceType: SourceType = SourceType.RECEIPT,
    val sourceItemId: String? = null,
    val sourceReceiptId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

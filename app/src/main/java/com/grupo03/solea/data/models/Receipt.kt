package com.grupo03.solea.data.models

import java.time.LocalDateTime

/**
 * Represents a scanned or manually entered receipt.
 *
 * A Receipt contains information about a purchase from a store/establishment
 * and can contain multiple items. Receipts can be created through the
 * camera scanning feature or entered manually.
 *
 * @property id Unique identifier for the receipt
 * @property description Description or establishment name
 * @property datetime Date and time of the purchase
 * @property currency Currency code used for this receipt
 * @property total Total amount on the receipt
 * @property createdAt Timestamp when this receipt record was created
 */
data class Receipt(
    val id: String = "",
    val description: String = "",
    val datetime: LocalDateTime = LocalDateTime.now(),
    val currency: String = "USD",
    val total: Double = 0.0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

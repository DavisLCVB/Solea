package com.grupo03.solea.data.models

import java.time.LocalDateTime

/**
 * Represents a purchased item.
 *
 * An Item can exist as a standalone purchase or be part of a Receipt.
 * When part of a receipt, the receiptId field will be set.
 *
 * @property id Unique identifier for the item
 * @property receiptId Optional ID of the receipt this item belongs to (null for standalone items)
 * @property description Description or name of the item
 * @property quantity Quantity purchased
 * @property currency Currency code for pricing
 * @property unitPrice Price per unit
 * @property totalPrice Total price for this item (quantity * unitPrice)
 * @property category Category classification for this item
 * @property createdAt Timestamp when this item record was created
 */
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

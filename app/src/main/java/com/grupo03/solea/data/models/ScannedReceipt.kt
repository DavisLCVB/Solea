package com.grupo03.solea.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * Response from the AI receipt scanner service.
 *
 * This represents the top-level response from the external receipt scanning API.
 *
 * @property receipt The scanned receipt data extracted by the AI
 */
data class ScannedReceiptResponse(
    @SerializedName("receipt")
    val receipt: ScannedReceiptData
)

/**
 * Data extracted from a scanned receipt by the AI service.
 *
 * Contains all the information parsed from the receipt image, including
 * establishment details, items, and suggested categorization.
 *
 * @property establishmentName Name of the store or establishment
 * @property date Date from the receipt in ISO 8601 format (nullable if not detected)
 * @property total Total amount on the receipt
 * @property currency Currency code detected from the receipt
 * @property items List of items extracted from the receipt
 * @property suggestedCategory AI-suggested category based on the items and establishment
 * @property confidence Confidence level of the AI extraction (0.0 to 1.0)
 */
data class ScannedReceiptData(
    @SerializedName("establishmentName")
    val establishmentName: String = "",

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("total")
    val total: Double = 0.0,

    @SerializedName("currency")
    val currency: String = "USD",

    @SerializedName("items")
    val items: List<ScannedItemData> = emptyList(),

    @SerializedName("suggestedCategory")
    val suggestedCategory: String? = null,

    @SerializedName("confidence")
    val confidence: Double = 0.0
)

/**
 * Data for an individual item extracted from a scanned receipt.
 *
 * Represents a single line item from the receipt as detected by the AI.
 *
 * @property description Item description or name
 * @property quantity Quantity of the item
 * @property unitPrice Price per unit
 * @property totalPrice Total price for this item
 */
data class ScannedItemData(
    @SerializedName("description")
    val description: String = "",

    @SerializedName("quantity")
    val quantity: Double = 1.0,

    @SerializedName("unitPrice")
    val unitPrice: Double = 0.0,

    @SerializedName("totalPrice")
    val totalPrice: Double = 0.0
)

/**
 * Editable version of a scanned receipt for user review and correction.
 *
 * After scanning, this allows the user to review and modify the AI-extracted data
 * before creating the final expense movement.
 *
 * @property establishmentName Name of the establishment (editable)
 * @property date Date of the purchase (editable)
 * @property total Total amount as string for editing
 * @property currency Currency code
 * @property items List of editable items
 * @property suggestedCategory AI-suggested category
 * @property confidence AI confidence level
 */
data class EditableScannedReceipt(
    val establishmentName: String = "",
    val date: LocalDateTime? = null,
    val total: String = "",
    val currency: String = "USD",
    val items: List<EditableScannedItem> = emptyList(),
    val suggestedCategory: String? = null,
    val confidence: Double = 0.0
)

/**
 * Editable version of a scanned item for user review and correction.
 *
 * Allows users to modify individual item details before final submission.
 *
 * @property id Temporary unique identifier for UI purposes
 * @property description Item description (editable)
 * @property quantity Quantity as string for editing
 * @property unitPrice Unit price as string for editing
 */
data class EditableScannedItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val description: String = "",
    val quantity: String = "",
    val unitPrice: String = ""
)

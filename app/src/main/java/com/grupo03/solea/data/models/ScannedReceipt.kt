package com.grupo03.solea.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * Response from AI receipt scanner service
 */
data class ScannedReceiptResponse(
    @SerializedName("receipt")
    val receipt: ScannedReceiptData
)

data class ScannedReceiptData(
    @SerializedName("establishmentName")
    val establishmentName: String = "",

    @SerializedName("date")
    val date: String? = null, // ISO 8601 format

    @SerializedName("total")
    val total: Double = 0.0,

    @SerializedName("currency")
    val currency: String = "USD",

    @SerializedName("items")
    val items: List<ScannedItemData> = emptyList(),

    @SerializedName("confidence")
    val confidence: Double = 0.0
)

data class ScannedItemData(
    @SerializedName("description")
    val description: String = "",

    @SerializedName("quantity")
    val quantity: Double = 1.0,

    @SerializedName("unitPrice")
    val unitPrice: Double = 0.0,

    @SerializedName("totalPrice")
    val totalPrice: Double = 0.0,

    @SerializedName("category")
    val category: String? = null
)

/**
 * Editable version for user to modify before creating movement
 */
data class EditableScannedReceipt(
    val establishmentName: String = "",
    val date: LocalDateTime? = null,
    val total: String = "",
    val currency: String = "USD",
    val items: List<EditableScannedItem> = emptyList(),
    val confidence: Double = 0.0
)

data class EditableScannedItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val description: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val category: String = ""
)

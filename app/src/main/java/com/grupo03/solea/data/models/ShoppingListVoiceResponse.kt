package com.grupo03.solea.data.models

import com.google.gson.annotations.SerializedName

/**
 * Response from the AI voice analyzer service for shopping lists.
 *
 * This represents the top-level response from the external audio analysis API
 * when processing voice notes for shopping lists.
 *
 * @property shoppingList The analyzed shopping list data extracted by the AI
 */
data class ShoppingListVoiceResponse(
    @SerializedName("shoppingList")
    val shoppingList: ShoppingListVoiceData
)

/**
 * Data extracted from an analyzed voice note for shopping lists by the AI service.
 *
 * Contains all the shopping items parsed from the audio recording.
 *
 * @property listName Suggested name for the shopping list (optional)
 * @property items List of shopping items extracted from the voice note
 * @property transcription Full transcription of the audio
 * @property confidence Confidence level of the AI extraction (0.0 to 1.0)
 */
data class ShoppingListVoiceData(
    @SerializedName("listName")
    val listName: String? = null,

    @SerializedName("items")
    val items: List<ShoppingItemVoiceData> = emptyList(),

    @SerializedName("transcription")
    val transcription: String = "",

    @SerializedName("confidence")
    val confidence: Double = 0.0
)

/**
 * Individual shopping item extracted from voice note.
 *
 * @property name Name or description of the item
 * @property quantity Expected quantity to buy (optional, defaults to 1.0)
 * @property estimatedPrice Optional estimated price (for budgeting)
 */
data class ShoppingItemVoiceData(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("quantity")
    val quantity: Double = 1.0,

    @SerializedName("estimatedPrice")
    val estimatedPrice: Double? = null
)


package com.grupo03.solea.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * Response from the AI audio analyzer service.
 *
 * This represents the top-level response from the external audio analysis API.
 *
 * @property voiceNote The analyzed voice note data extracted by the AI
 */
data class AnalyzedVoiceNoteResponse(
    @SerializedName("voiceNote")
    val voiceNote: AnalyzedVoiceNoteData
)

/**
 * Data extracted from an analyzed voice note by the AI service.
 *
 * Contains all the financial information parsed from the audio recording,
 * including amount, description, and suggested categorization.
 *
 * @property transcription Full transcription of the audio
 * @property amount Monetary amount extracted from the voice note
 * @property description Description or concept of the expense/income
 * @property movementType Type of movement: "expense" or "income"
 * @property date Date mentioned in the voice note in ISO 8601 format (nullable if not detected)
 * @property currency Currency code detected from the voice note
 * @property suggestedCategory AI-suggested category based on the description
 * @property confidence Confidence level of the AI extraction (0.0 to 1.0)
 */
data class AnalyzedVoiceNoteData(
    @SerializedName("transcription")
    val transcription: String = "",

    @SerializedName("amount")
    val amount: Double = 0.0,

    @SerializedName("description")
    val description: String = "",

    @SerializedName("movementType")
    val movementType: String = "expense", // "expense" or "income"

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("currency")
    val currency: String = "USD",

    @SerializedName("suggestedCategory")
    val suggestedCategory: String? = null,

    @SerializedName("confidence")
    val confidence: Double = 0.0
)

/**
 * Editable version of an analyzed voice note for user review and correction.
 *
 * After analysis, this allows the user to review and modify the AI-extracted data
 * before creating the final expense/income movement.
 *
 * @property transcription Full transcription of the audio
 * @property amount Monetary amount as string for editing
 * @property description Description or concept (editable)
 * @property movementType Type of movement: "expense" or "income"
 * @property date Date of the movement (editable)
 * @property currency Currency code
 * @property suggestedCategory AI-suggested category
 * @property confidence AI confidence level
 */
data class EditableVoiceNote(
    val transcription: String = "",
    val amount: String = "",
    val description: String = "",
    val movementType: String = "expense",
    val date: LocalDateTime? = null,
    val currency: String = "USD",
    val suggestedCategory: String? = null,
    val confidence: Double = 0.0
)

package com.grupo03.solea.data.services.interfaces

import com.grupo03.solea.data.models.AnalyzedVoiceNoteResponse
import com.grupo03.solea.data.models.Category
import java.io.File

/**
 * Service interface for analyzing audio files and extracting financial data using AI.
 *
 * This service communicates with an external AI-powered audio analysis service to extract
 * structured financial data from voice notes, including amounts, descriptions, and suggested categories.
 */
interface AudioAnalyzerService {

    /**
     * Analyzes an audio file and extracts financial data using AI.
     *
     * The AI service transcribes the audio and extracts:
     * - Full transcription
     * - Monetary amount
     * - Description/concept of the expense or income
     * - Movement type (expense/income)
     * - Date (if mentioned)
     * - Suggested category based on the description
     *
     * @param audioFile The audio file to analyze (supported formats: MP3, WAV, OGG, WebM)
     * @param categories List of available categories (both default and user-created)
     *                   to help the AI suggest the most appropriate category
     * @param defaultCurrency Default currency code for the device locale (e.g., "PEN", "PEN")
     * @param language User's preferred language code (e.g., "en", "es") for AI responses
     * @return Result containing AnalyzedVoiceNoteResponse with extracted data on success,
     *         or an error if analysis fails
     */
    suspend fun analyzeAudio(
        audioFile: File,
        categories: List<Category> = emptyList(),
        defaultCurrency: String = "PEN",
        language: String = "es"
    ): Result<AnalyzedVoiceNoteResponse>
}

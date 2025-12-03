package com.grupo03.solea.data.services.interfaces

import java.io.File

/**
 * Service interface for processing voice notes to create shopping lists.
 */
interface ShoppingListVoiceService {
    /**
     * Analyzes an audio file containing a shopping list voice note
     * and extracts shopping items.
     *
     * @param audioFile The audio file to analyze
     * @return Result containing the parsed shopping list data or an error
     */
    suspend fun analyzeShoppingListAudio(audioFile: File): Result<com.grupo03.solea.data.models.ShoppingListVoiceResponse>
}


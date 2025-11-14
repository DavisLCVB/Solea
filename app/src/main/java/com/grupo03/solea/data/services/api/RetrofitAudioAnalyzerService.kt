package com.grupo03.solea.data.services.api

import android.util.Log
import com.google.gson.Gson
import com.grupo03.solea.data.models.AnalyzedVoiceNoteResponse
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.services.interfaces.AudioAnalyzerService
import com.grupo03.solea.utils.AIConstants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Retrofit API interface for audio analysis
 */
interface AudioAnalyzerApi {
    @Multipart
    @POST("analyze-audio")
    suspend fun analyzeAudio(
        @Part file: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Response<GeminiApiResponse>
}

/**
 * Implementation of AudioAnalyzerService using Retrofit
 */
class RetrofitAudioAnalyzerService : AudioAnalyzerService {

    private val api: AudioAnalyzerApi
    private val gson = Gson()

    // Base prompt template for audio analysis
    private fun buildPrompt(categories: List<Category>, defaultCurrency: String): String {
        val categoriesSection = if (categories.isNotEmpty() && categories.size <= 15) {
            val categoriesList = categories.take(15).joinToString(", ") { "\"${it.name}\"" }
            "Categories: $categoriesList. "
        } else {
            ""
        }

        return """
Transcribe and extract financial data from this audio. Return ONLY valid JSON, no markdown.

Rules:
- Don't invent data. Use null if uncertain.
- Currency: default "$defaultCurrency" unless mentioned.
- Date: ISO 8601 format if mentioned, else null.
- movementType: "expense" or "income"

JSON format:
{
  "voiceNote": {
    "transcription": "full audio transcription",
    "amount": 0.00,
    "description": "brief description",
    "movementType": "expense",
    "date": null,
    "currency": "$defaultCurrency",
    "suggestedCategory": "category name or null",
    "confidence": 0.95
  }
}

${categoriesSection}Return ONLY the JSON.
""".trimIndent()
    }

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(AIConstants.RECEIPT_SCANNER_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AudioAnalyzerApi::class.java)
    }

    override suspend fun analyzeAudio(
        audioFile: File,
        categories: List<Category>,
        defaultCurrency: String
    ): Result<AnalyzedVoiceNoteResponse> {
        return try {
            Log.d("AudioAnalyzer", "Analyzing audio: ${audioFile.name} (${audioFile.length()} bytes)")

            // Build prompt with categories and default currency
            val promptText = buildPrompt(categories, defaultCurrency)

            // Determine MIME type based on file extension
            val mimeType = when {
                audioFile.name.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
                audioFile.name.endsWith(".wav", ignoreCase = true) -> "audio/wav"
                audioFile.name.endsWith(".ogg", ignoreCase = true) -> "audio/ogg"
                audioFile.name.endsWith(".webm", ignoreCase = true) -> "audio/webm"
                audioFile.name.endsWith(".m4a", ignoreCase = true) -> "audio/mp4"
                else -> "audio/mpeg"
            }

            val mediaType = mimeType.toMediaTypeOrNull()
                ?: return Result.failure(Exception("Invalid file type: $mimeType"))

            // Validate file size (max 10MB according to API docs)
            val maxFileSize = 10 * 1024 * 1024 // 10MB
            if (audioFile.length() > maxFileSize) {
                return Result.failure(Exception("File size exceeds maximum allowed size (10MB)"))
            }

            // Create multipart request
            val requestFile = audioFile.asRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)

            val promptMediaType = "text/plain; charset=utf-8".toMediaTypeOrNull()
            val promptBody = promptText.toRequestBody(promptMediaType)

            val response = api.analyzeAudio(filePart, promptBody)

            if (response.isSuccessful) {
                val result = response.body()

                if (result?.error != null) {
                    Log.e("AudioAnalyzer", "API error: ${result.error}")
                    return Result.failure(Exception("Server error: ${result.error}"))
                }

                val jsonString = result?.result
                if (jsonString.isNullOrEmpty()) {
                    Log.e("AudioAnalyzer", "Empty response from server")
                    return Result.failure(Exception("Empty response from server"))
                }

                try {
                    // Clean up JSON response (remove markdown formatting if present)
                    val cleanedJson = jsonString.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val voiceNoteResponse = gson.fromJson(cleanedJson, AnalyzedVoiceNoteResponse::class.java)
                    Log.d("AudioAnalyzer", "Audio analyzed: ${voiceNoteResponse.voiceNote.description}, ${voiceNoteResponse.voiceNote.amount} ${voiceNoteResponse.voiceNote.currency}")

                    Result.success(voiceNoteResponse)
                } catch (e: Exception) {
                    Log.e("AudioAnalyzer", "JSON parsing error: ${e.message}")
                    Result.failure(Exception("Failed to parse audio data: ${e.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AudioAnalyzer", "HTTP ${response.code()}: $errorBody")

                val detailedError = when (response.code()) {
                    400 -> "Invalid request - Please verify the audio file is valid"
                    401 -> "Unauthorized - Authentication problem"
                    413 -> "Audio file too large - Maximum size is 10MB"
                    500 -> "Internal server error - Try again in a few minutes"
                    503 -> "Service unavailable - Server is busy"
                    else -> "HTTP error ${response.code()}: ${response.message()}"
                }

                Result.failure(Exception(detailedError))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("AudioAnalyzer", "Timeout: ${e.message}")
            Result.failure(Exception("Request timeout. The server took too long to respond. Try with a shorter audio or check your internet connection."))
        } catch (e: java.net.ConnectException) {
            Log.e("AudioAnalyzer", "Connection failed: ${e.message}")
            Result.failure(Exception("Connection error. Could not connect to the server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e("AudioAnalyzer", "Unexpected error: ${e.message}")
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
}

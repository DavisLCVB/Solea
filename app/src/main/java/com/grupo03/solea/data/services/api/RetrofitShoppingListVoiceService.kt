package com.grupo03.solea.data.services.api

import android.util.Log
import com.google.gson.Gson
import com.grupo03.solea.data.models.ShoppingListVoiceResponse
import com.grupo03.solea.data.services.interfaces.ShoppingListVoiceService
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
 * Retrofit API interface for shopping list voice analysis
 */
interface ShoppingListVoiceApi {
    @Multipart
    @POST("analyze-audio")
    suspend fun analyzeShoppingListAudio(
        @Part file: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Response<GeminiApiResponse>
}

/**
 * Implementation of ShoppingListVoiceService using Retrofit
 */
class RetrofitShoppingListVoiceService : ShoppingListVoiceService {

    private val api: ShoppingListVoiceApi
    private val gson = Gson()

    private fun buildPrompt(): String {
        return """
Extract shopping list items from this audio. Return ONLY valid JSON, no markdown.

Rules:
- Extract all items mentioned in the audio
- Item names should be clear and specific (e.g., "Leche" not "milk product")
- Quantity defaults to 1.0 if not mentioned
- estimatedPrice is optional, only include if mentioned
- listName is optional, extract if a name/title is mentioned for the list

JSON format:
{
  "shoppingList": {
    "listName": "optional list name or null",
    "items": [
      {
        "name": "item name",
        "quantity": 1.0,
        "estimatedPrice": null
      }
    ],
    "transcription": "full audio transcription",
    "confidence": 0.95
  }
}

Return ONLY the JSON, no markdown, no code blocks.
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

        api = retrofit.create(ShoppingListVoiceApi::class.java)
    }

    override suspend fun analyzeShoppingListAudio(audioFile: File): Result<ShoppingListVoiceResponse> {
        return try {
            Log.d("ShoppingListVoice", "Analyzing audio: ${audioFile.name} (${audioFile.length()} bytes)")

            val promptText = buildPrompt()

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

            val maxFileSize = 10 * 1024 * 1024
            if (audioFile.length() > maxFileSize) {
                return Result.failure(Exception("File size exceeds maximum allowed size (10MB)"))
            }

            val requestFile = audioFile.asRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)

            val promptMediaType = "text/plain; charset=utf-8".toMediaTypeOrNull()
            val promptBody = promptText.toRequestBody(promptMediaType)

            val response = api.analyzeShoppingListAudio(filePart, promptBody)

            if (response.isSuccessful) {
                val result = response.body()

                if (result?.error != null) {
                    Log.e("ShoppingListVoice", "API error: ${result.error}")
                    return Result.failure(Exception("Server error: ${result.error}"))
                }

                val jsonString = result?.result
                if (jsonString.isNullOrEmpty()) {
                    Log.e("ShoppingListVoice", "Empty response from server")
                    return Result.failure(Exception("Empty response from server"))
                }

                try {
                    val cleanedJson = jsonString.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val shoppingListResponse = gson.fromJson(cleanedJson, ShoppingListVoiceResponse::class.java)
                    Log.d("ShoppingListVoice", "Audio analyzed: ${shoppingListResponse.shoppingList.items.size} items extracted")

                    Result.success(shoppingListResponse)
                } catch (e: Exception) {
                    Log.e("ShoppingListVoice", "JSON parsing error: ${e.message}")
                    Result.failure(Exception("Failed to parse shopping list data: ${e.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ShoppingListVoice", "HTTP ${response.code()}: $errorBody")

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
            Log.e("ShoppingListVoice", "Timeout: ${e.message}")
            Result.failure(Exception("Request timeout. The server took too long to respond. Try with a shorter audio or check your internet connection."))
        } catch (e: java.net.ConnectException) {
            Log.e("ShoppingListVoice", "Connection failed: ${e.message}")
            Result.failure(Exception("Connection error. Could not connect to the server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e("ShoppingListVoice", "Unexpected error: ${e.message}")
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
}


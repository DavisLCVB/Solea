package com.grupo03.solea.data.services.api

import android.util.Log
import com.google.gson.Gson
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.ScannedReceiptResponse
import com.grupo03.solea.data.services.interfaces.ReceiptScannerService
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
 * API response wrapper from Gemini service
 */
data class GeminiApiResponse(
    val result: String?,
    val error: String?
)

/**
 * Retrofit API interface for receipt scanning
 */
interface ReceiptScannerApi {
    @Multipart
    @POST("analyze-image")
    suspend fun scanReceipt(
        @Part image: MultipartBody.Part,
        @Part("prompt") prompt: String
    ): Response<GeminiApiResponse>
}

/**
 * Implementation of ReceiptScannerService using Retrofit
 */
class RetrofitReceiptScannerService : ReceiptScannerService {

    private val api: ReceiptScannerApi
    private val gson = Gson()

    // Base prompt template for receipt scanning
    private fun buildPrompt(categories: List<Category>, defaultCurrency: String): String {
        val categoriesSection = if (categories.isNotEmpty()) {
            val categoriesList = categories.joinToString(", ") { category -> "\"${category.name}\"" }
            "Suggest ONE category from: $categoriesList (or a new one if none fit)."
        } else {
            "Suggest ONE category (e.g., \"Groceries\", \"Restaurant\")."
        }

        return """
Extract receipt data and return ONLY JSON. Do not invent data - use null/"" if not visible.

Rules:
1. Currency: Use receipt's currency or default to "$defaultCurrency"
2. Date: ISO 8601 format (YYYY-MM-DDTHH:MM:SS-05:00) or null
3. Numbers: 2 decimals, quantity defaults to 1.0 if not shown
4. Items: Calculate totalPrice = quantity × unitPrice

JSON format:
{
  "receipt": {
    "establishmentName": "",
    "date": null,
    "total": 0.00,
    "currency": "$defaultCurrency",
    "items": [{"description": "", "quantity": 1.0, "unitPrice": 0.00, "totalPrice": 0.00}],
    "suggestedCategory": null,
    "confidence": 0.95
  }
}

$categoriesSection
Return ONLY JSON, no markdown.
""".trimIndent()
    }

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(AIConstants.RECEIPT_SCANNER_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ReceiptScannerApi::class.java)
    }

    override suspend fun scanReceipt(imageFile: File, categories: List<Category>, defaultCurrency: String): Result<ScannedReceiptResponse> {
        return try {
            Log.d("ReceiptScanner", "Scanning receipt: ${imageFile.name} (${imageFile.length()} bytes)")

            // Build prompt with categories and default currency
            val promptText = buildPrompt(categories, defaultCurrency)

            // Determine MIME type based on file extension
            val mimeType = when {
                imageFile.name.endsWith(".png", ignoreCase = true) -> "image/png"
                imageFile.name.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
                imageFile.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                imageFile.name.endsWith(".webp", ignoreCase = true) -> "image/webp"
                else -> "image/jpeg"
            }

            val mediaType = mimeType.toMediaTypeOrNull()
                ?: return Result.failure(Exception("Invalid file type: $mimeType"))

            // Create multipart request
            val requestFile = imageFile.asRequestBody(mediaType)
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

            Log.d("ReceiptScanner", "Enviando request al backend...")
            Log.d("ReceiptScanner", "Prompt length: ${promptText.length} chars")
            val response = api.scanReceipt(imagePart, promptText)

            if (response.isSuccessful) {
                val result = response.body()

                if (result?.error != null) {
                    Log.e("ReceiptScanner", "API error: ${result.error}")
                    return Result.failure(Exception("Server error: ${result.error}"))
                }

                val jsonString = result?.result
                if (jsonString.isNullOrEmpty()) {
                    Log.e("ReceiptScanner", "Empty response from server")
                    return Result.failure(Exception("Empty response from server"))
                }

                try {
                    // Clean up JSON response (remove markdown formatting if present)
                    val cleanedJson = jsonString.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val receiptResponse = gson.fromJson(cleanedJson, ScannedReceiptResponse::class.java)
                    Log.d("ReceiptScanner", "Receipt scanned: ${receiptResponse.receipt.establishmentName}, ${receiptResponse.receipt.items.size} items")

                    Result.success(receiptResponse)
                } catch (e: Exception) {
                    Log.e("ReceiptScanner", "JSON parsing error: ${e.message}")
                    Result.failure(Exception("Failed to parse receipt data: ${e.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ReceiptScanner", "HTTP ${response.code()}: $errorBody")

                val detailedError = when (response.code()) {
                    400 -> "Invalid request - Please verify the image is valid"
                    401 -> "Unauthorized - Authentication problem"
                    413 -> "Image too large - Try a smaller image"
                    500 -> "Internal server error - Try again in a few minutes"
                    503 -> "Service unavailable - Server is busy"
                    else -> "HTTP error ${response.code()}: ${response.message()}"
                }

                Result.failure(Exception(detailedError))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("ReceiptScanner", "Timeout: ${e.message}")
            Result.failure(Exception("Request timeout. The server took too long to respond. Try with a smaller image or check your internet connection."))
        } catch (e: java.net.ConnectException) {
            Log.e("ReceiptScanner", "Connection failed: ${e.message}")
            Result.failure(Exception("Connection error. Could not connect to the server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e("ReceiptScanner", "Unexpected error: ${e.message}")
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
}

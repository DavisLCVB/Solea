package com.grupo03.solea.data.services.api

import android.util.Log
import com.google.gson.Gson
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
        @Part file: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Response<GeminiApiResponse>
}

/**
 * Implementation of ReceiptScannerService using Retrofit
 */
class RetrofitReceiptScannerService : ReceiptScannerService {

    private val api: ReceiptScannerApi
    private val gson = Gson()

    // Optimized prompt for receipt scanning
    private val promptText = """
You are a reliable receipt and sales ticket data extractor. Your job is to read receipts (image or OCR text) and return **only JSON** with structured and enriched information. **Do not invent data**. If something is not visible or does not exist, **leave it empty** (`null`, `""` or `[]` as appropriate).

## General Rules (Very Important)

1. **Do not invent**: if a value is not explicit or cannot be inferred with high confidence, leave it empty (`null`, `""` or `[]`).
2. **Currency**: if recognized, include it (default to local currency if document clearly suggests it). If not visible, leave `currency` empty.
3. **Dates and times**: return `date` in **ISO 8601** format with local timezone: `YYYY-MM-DDTHH:MM:SS-05:00` if deducible; if not, leave `null`.
4. **Numbers**: use decimal point; normal rounding to 2 decimals when applicable.
5. **Do not create discounts, taxes, or charges if they are not visible.**

## Required JSON Output Format

Return ONLY the JSON structured exactly as follows (no additional text, comments, or markdown):

{
  "receipt": {
    "establishmentName": "Store or establishment name (string, empty if not visible)",
    "date": "Transaction date in ISO 8601 format or null",
    "total": 0.00,
    "currency": "USD",
    "items": [
      {
        "description": "Item description (string)",
        "quantity": 1.0,
        "unitPrice": 0.00,
        "totalPrice": 0.00,
        "category": "Inferred category if possible, otherwise null"
      }
    ],
    "confidence": 0.95
  }
}

## Important Notes:

- `establishmentName`: Extract from receipt header. Leave empty if not visible.
- `date`: Parse date and time if visible, format as ISO 8601. Use null if unclear.
- `total`: Total amount paid. Must be a number.
- `currency`: Three-letter currency code (USD, PEN, EUR, etc.). Leave empty if unknown.
- `items`: Array of line items. Each item must have:
  - `description`: What was purchased
  - `quantity`: Numeric quantity (default 1.0 if not shown)
  - `unitPrice`: Price per unit
  - `totalPrice`: Total for this line item (quantity × unitPrice)
  - `category`: Optional. Infer from description if possible (e.g., "Food", "Beverages", "Household")
- `confidence`: Your confidence level in the extraction (0.0 to 1.0)

## Extraction Guidelines:

1. Focus on line items with prices
2. If quantity is not shown, assume 1.0
3. Calculate totalPrice = quantity × unitPrice
4. Extract establishment name from header/logo area
5. Look for date/time near top or bottom of receipt
6. Total should match the final amount paid
7. Currency should match country/region indicators

Return ONLY the JSON object. No markdown, no explanations.
""".trimIndent()

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d("ReceiptScanner", "URL: ${request.url}")
                Log.d("ReceiptScanner", "Method: ${request.method}")

                val startTime = System.currentTimeMillis()
                val response = chain.proceed(request)
                val endTime = System.currentTimeMillis()

                Log.d("ReceiptScanner", "Response code: ${response.code}")
                Log.d("ReceiptScanner", "Response time: ${endTime - startTime}ms")

                response
            }
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

    override suspend fun scanReceipt(imageFile: File): Result<ScannedReceiptResponse> {
        return try {
            Log.d("ReceiptScanner", "Starting receipt scan for file: ${imageFile.name}")
            Log.d("ReceiptScanner", "File size: ${imageFile.length()} bytes")

            // Determine MIME type based on file extension
            val mimeType = when {
                imageFile.name.endsWith(".png", ignoreCase = true) -> "image/png"
                imageFile.name.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
                imageFile.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                imageFile.name.endsWith(".webp", ignoreCase = true) -> "image/webp"
                else -> "image/jpeg"
            }

            val mediaType = mimeType.toMediaTypeOrNull()
            if (mediaType == null) {
                Log.e("ReceiptScanner", "Invalid MIME type: $mimeType")
                return Result.failure(Exception("Invalid file type: $mimeType"))
            }

            // Create multipart request
            val requestFile = imageFile.asRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val promptMediaType = "text/plain; charset=utf-8".toMediaTypeOrNull()
            val promptBody = promptText.toRequestBody(promptMediaType)

            Log.d("ReceiptScanner", "Sending request to API")
            val response = api.scanReceipt(filePart, promptBody)

            Log.d("ReceiptScanner", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val result = response.body()

                if (result?.error != null) {
                    Log.e("ReceiptScanner", "API returned error: ${result.error}")
                    return Result.failure(Exception("Server error: ${result.error}"))
                }

                val jsonString = result?.result
                if (jsonString.isNullOrEmpty()) {
                    Log.e("ReceiptScanner", "Empty JSON response")
                    return Result.failure(Exception("Empty response from server"))
                }

                try {
                    Log.d("ReceiptScanner", "Raw JSON (first 300 chars): ${jsonString.take(300)}")

                    // Clean up JSON response (remove markdown formatting if present)
                    val cleanedJson = jsonString.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    Log.d("ReceiptScanner", "Parsing JSON response")
                    val receiptResponse = gson.fromJson(cleanedJson, ScannedReceiptResponse::class.java)

                    Log.d("ReceiptScanner", "Successfully parsed receipt")
                    Log.d("ReceiptScanner", "Establishment: ${receiptResponse.receipt.establishmentName}")
                    Log.d("ReceiptScanner", "Total: ${receiptResponse.receipt.total}")
                    Log.d("ReceiptScanner", "Items: ${receiptResponse.receipt.items.size}")

                    Result.success(receiptResponse)
                } catch (e: Exception) {
                    Log.e("ReceiptScanner", "JSON parsing error", e)
                    Log.e("ReceiptScanner", "Failed JSON: $jsonString")
                    Result.failure(Exception("Failed to parse receipt data: ${e.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ReceiptScanner", "HTTP Error ${response.code()}: ${response.message()}")
                Log.e("ReceiptScanner", "Error body: $errorBody")

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
            Log.e("ReceiptScanner", "Socket timeout", e)
            Result.failure(Exception("Request timeout. The server took too long to respond. Try with a smaller image or check your internet connection."))
        } catch (e: java.net.ConnectException) {
            Log.e("ReceiptScanner", "Connection failed", e)
            Result.failure(Exception("Connection error. Could not connect to the server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e("ReceiptScanner", "Unexpected error", e)
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
}

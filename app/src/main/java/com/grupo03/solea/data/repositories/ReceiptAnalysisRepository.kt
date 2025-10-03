package com.grupo03.solea.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.grupo03.solea.data.models.ReceiptAnalysis
import com.grupo03.solea.data.services.ReceiptAnalysisResponse
import com.grupo03.solea.data.services.ReceiptAnalysisService
import com.grupo03.solea.utils.JsonParsingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

interface IReceiptAnalysisRepository {
    suspend fun analyzeReceiptImage(imageUri: Uri, context: Context): Result<ReceiptAnalysis>
}

class ReceiptAnalysisRepository : IReceiptAnalysisRepository {
    
    private fun compressImage(file: File, maxSizeKB: Int = 1024): File {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap == null) {
                Log.w("ReceiptAnalysis", "Could not decode image, using original")
                return file
            }
            
            var quality = 85
            var compressedData: ByteArray
            
            do {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedData = outputStream.toByteArray()
                quality -= 10
            } while (compressedData.size > maxSizeKB * 1024 && quality > 10)
            
            val compressedFile = File(file.parent, "compressed_${file.name}")
            FileOutputStream(compressedFile).use { fos ->
                fos.write(compressedData)
            }
            
            Log.d("ReceiptAnalysis", "Image compressed: ${file.length()} -> ${compressedFile.length()} bytes (${quality + 10}% quality)")
            return compressedFile
        } catch (e: Exception) {
            Log.e("ReceiptAnalysis", "Error compressing image, using original", e)
            return file
        }
    }
    
    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d("HTTP_REQUEST", "URL: ${request.url}")
            Log.d("HTTP_REQUEST", "Method: ${request.method}")
            Log.d("HTTP_REQUEST", "Headers: ${request.headers}")
            
            val startTime = System.currentTimeMillis()
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()
            
            Log.d("HTTP_RESPONSE", "Code: ${response.code}")
            Log.d("HTTP_RESPONSE", "Message: ${response.message}")
            Log.d("HTTP_RESPONSE", "Time: ${endTime - startTime}ms")
            Log.d("HTTP_RESPONSE", "Headers: ${response.headers}")
            
            if (!response.isSuccessful) {
                val errorBodyString = response.peekBody(2048).string()
                Log.e("HTTP_ERROR", "Response body: $errorBodyString")
            }
            
            response
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)  // Aumentado a 2 minutos
        .writeTimeout(120, TimeUnit.SECONDS) // Aumentado a 2 minutos
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gemini-py.onrender.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val service = retrofit.create(ReceiptAnalysisService::class.java)
    
    private val promptText = """
**Rol:** Eres un extractor confiable de datos de boletas y tickets de venta en Perú. Tu trabajo es leer boletas (imagen o texto OCR) y devolver **solo JSON** con la información estructurada y enriquecida, **sin inventar datos**. Si algo no se ve o no existe, **déjalo vacío** (`null`, `""` o `[]` según corresponda). No agregues comentarios, no expliques: **responde únicamente el JSON**.

## Reglas generales (muy importante)

1. **No inventes**: si un valor no está explícito o no puede inferirse con alta confianza, déjalo vacío (`null`, `""` o `[]`).
2. **Moneda**: si se reconoce, inclúyela (por defecto `PEN` si el documento lo sugiere claramente). Si no se ve, deja `currency` vacío.
3. **Fechas y horas**: devuelve
   * `transaction_details.iso_datetime_local` en **ISO 8601** con zona de Lima: `YYYY-MM-DDTHH:MM:SS-05:00` si fuera deducible; si no, deja `null`.
   * Conserva además la forma original si está impresa (ej. `22/08/2025`, `20:15`).
4. **Números**: usa punto decimal; redondeo normal a 2 decimales cuando aplique.
5. **Descuentos, impuestos, cargos**: si no aparecen, deja el campo vacío o en `0.00` únicamente si la línea explícitamente indica "0.00". **No crees descuentos ni IGV si no están visibles.**

Devuelve únicamente el JSON estructurado según el formato especificado en el prompt completo.
""".trimIndent()
    
    override suspend fun analyzeReceiptImage(imageUri: Uri, context: Context): Result<ReceiptAnalysis> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ReceiptAnalysis", "Starting image analysis for URI: $imageUri")
                
                // Convert URI to File
                val inputStream = context.contentResolver.openInputStream(imageUri)
                if (inputStream == null) {
                    Log.e("ReceiptAnalysis", "Failed to open input stream for URI: $imageUri")
                    return@withContext Result.failure(Exception("No se pudo abrir la imagen"))
                }
                
                val tempFile = File.createTempFile("receipt", ".jpg", context.cacheDir)
                Log.d("ReceiptAnalysis", "Created temp file: ${tempFile.absolutePath}")
                
                inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                val fileSize = tempFile.length()
                Log.d("ReceiptAnalysis", "Original file size: $fileSize bytes")
                
                if (fileSize == 0L) {
                    Log.e("ReceiptAnalysis", "Temp file is empty!")
                    tempFile.delete()
                    return@withContext Result.failure(Exception("El archivo de imagen está vacío"))
                }
                
                // Compress image if larger than 2MB
                val finalFile = if (fileSize > 2 * 1024 * 1024) {
                    Log.d("ReceiptAnalysis", "Image is large ($fileSize bytes), compressing...")
                    compressImage(tempFile, 1024) // Compress to max 1MB
                } else {
                    tempFile
                }
                
                // Try to get MIME type from ContentResolver first, then fallback to file extension
                val mimeTypeFromUri = context.contentResolver.getType(imageUri)
                val mimeType = when {
                    !mimeTypeFromUri.isNullOrEmpty() && mimeTypeFromUri.startsWith("image/") -> mimeTypeFromUri
                    finalFile.name.endsWith(".png", ignoreCase = true) -> "image/png"
                    finalFile.name.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
                    finalFile.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                    finalFile.name.endsWith(".webp", ignoreCase = true) -> "image/webp"
                    else -> "image/jpeg" // Default fallback
                }
                
                Log.d("ReceiptAnalysis", "MIME type from URI: $mimeTypeFromUri")
                Log.d("ReceiptAnalysis", "Using MIME type: $mimeType")
                
                // Validate MIME type
                val mediaType = mimeType.toMediaTypeOrNull()
                if (mediaType == null) {
                    Log.e("ReceiptAnalysis", "Invalid MIME type: $mimeType")
                    finalFile.delete()
                    if (finalFile != tempFile) tempFile.delete()
                    return@withContext Result.failure(Exception("Tipo de archivo no válido: $mimeType"))
                }
                
                // Create request body
                val requestFile = finalFile.asRequestBody(mediaType)
                val body = MultipartBody.Part.createFormData("file", finalFile.name, requestFile)
                
                val promptMediaType = "text/plain; charset=utf-8".toMediaTypeOrNull()
                val promptRequestBody = promptText.toRequestBody(promptMediaType)
                
                Log.d("ReceiptAnalysis", "Sending request to: ${retrofit.baseUrl()}analyze-image")
                Log.d("ReceiptAnalysis", "File name: ${finalFile.name}")
                Log.d("ReceiptAnalysis", "Final file size: ${finalFile.length()} bytes")
                Log.d("ReceiptAnalysis", "Media type: $mediaType")
                Log.d("ReceiptAnalysis", "Prompt length: ${promptText.length} characters")
                Log.d("ReceiptAnalysis", "Prompt media type: $promptMediaType")
                
                val response = service.analyzeReceipt(body, promptRequestBody)
                
                Log.d("ReceiptAnalysis", "Response code: ${response.code()}")
                Log.d("ReceiptAnalysis", "Response message: ${response.message()}")
                
                // Clean up temp files
                finalFile.delete()
                if (finalFile != tempFile) tempFile.delete()
                Log.d("ReceiptAnalysis", "Cleaned up temp files")
                
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("ReceiptAnalysis", "Response body: $result")
                    
                    if (result?.error != null) {
                        Log.e("ReceiptAnalysis", "API returned error: ${result.error}")
                        Result.failure(Exception("Error del servidor: ${result.error}"))
                    } else {
                        val jsonString = result?.result
                        if (jsonString.isNullOrEmpty()) {
                            Log.e("ReceiptAnalysis", "Empty JSON response")
                            Result.failure(Exception("Respuesta vacía del servidor"))
                        } else {
                            try {
                                Log.d("ReceiptAnalysis", "Raw JSON response (first 500 chars): ${jsonString.take(500)}")
                                
                                // Clean up the JSON if it has extra formatting
                                val cleanedJson = jsonString.trim()
                                    .removePrefix("```json")
                                    .removeSuffix("```")
                                    .trim()
                                
                                Log.d("ReceiptAnalysis", "Cleaned JSON (first 200 chars): ${cleanedJson.take(200)}")
                                
                                val receiptAnalysis = gson.fromJson(cleanedJson, ReceiptAnalysis::class.java)
                                Log.d("ReceiptAnalysis", "Successfully parsed receipt analysis")
                                Log.d("ReceiptAnalysis", "Store: ${receiptAnalysis.storeInfo?.name}")
                                Log.d("ReceiptAnalysis", "Total: ${receiptAnalysis.totals?.totalPrinted}")
                                Log.d("ReceiptAnalysis", "Items count: ${receiptAnalysis.items?.size ?: 0}")
                                Result.success(receiptAnalysis)
                            } catch (e: Exception) {
                                JsonParsingUtils.logParsingError("ReceiptAnalysis", e, jsonString)
                                val errorMessage = JsonParsingUtils.getParsingErrorMessage(e, jsonString)
                                
                                // Try to create a fallback minimal receipt analysis
                                try {
                                    val fallbackAnalysis = ReceiptAnalysis(
                                        source = null,
                                        storeInfo = null,
                                        issuerName = null,
                                        vendorName = null,
                                        documentType = null,
                                        documentNumber = null,
                                        transactionDetails = null,
                                        cashierInfo = null,
                                        items = emptyList(),
                                        lineItems = emptyList(),
                                        totals = null,
                                        summary = null,
                                        totalAmount = null,
                                        paymentInfo = null,
                                        paymentDetails = null,
                                        qualityChecks = null,
                                        mappingHints = null
                                    )
                                    Log.d("ReceiptAnalysis", "Using fallback analysis")
                                    Result.success(fallbackAnalysis)
                                } catch (fallbackException: Exception) {
                                    Log.e("ReceiptAnalysis", "Fallback also failed", fallbackException)
                                    Result.failure(Exception(errorMessage))
                                }
                            }
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ReceiptAnalysis", "HTTP Error ${response.code()}: ${response.message()}")
                    Log.e("ReceiptAnalysis", "Error body: $errorBody")
                    
                    val detailedError = when (response.code()) {
                        400 -> "Solicitud inválida - Verifica que la imagen sea válida"
                        401 -> "No autorizado - Problema de autenticación"
                        413 -> "Imagen demasiado grande - Intenta con una imagen más pequeña"
                        500 -> "Error interno del servidor - Intenta de nuevo en unos minutos"
                        503 -> "Servicio no disponible - El servidor está ocupado"
                        else -> "Error HTTP ${response.code()}: ${response.message()}"
                    }
                    
                    Result.failure(Exception("$detailedError\nDetalles técnicos: $errorBody"))
                }
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("ReceiptAnalysis", "Socket timeout during analysis", e)
                Result.failure(Exception("⏱️ Tiempo de espera agotado\n\nEl análisis está tomando más tiempo del esperado. Esto puede ocurrir cuando:\n• La imagen es muy grande\n• El servidor está sobrecargado\n• La conexión a internet es lenta\n\n💡 Sugerencias:\n• Intenta con una imagen más pequeña\n• Verifica tu conexión a internet\n• Intenta de nuevo en unos minutos"))
            } catch (e: java.net.ConnectException) {
                Log.e("ReceiptAnalysis", "Connection failed", e)
                Result.failure(Exception("🌐 Error de conexión\n\nNo se pudo conectar al servidor de análisis.\n\n💡 Sugerencias:\n• Verifica tu conexión a internet\n• Intenta de nuevo en unos minutos\n• El servidor podría estar en mantenimiento"))
            } catch (e: Exception) {
                Log.e("ReceiptAnalysis", "Exception during analysis", e)
                val errorMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "⏱️ Tiempo de espera agotado. Intenta con una imagen más pequeña o verifica tu conexión."
                    e.message?.contains("network", ignoreCase = true) == true -> 
                        "🌐 Error de red. Verifica tu conexión a internet."
                    else -> "Error inesperado: ${e.message}\nTipo: ${e.javaClass.simpleName}"
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }
}
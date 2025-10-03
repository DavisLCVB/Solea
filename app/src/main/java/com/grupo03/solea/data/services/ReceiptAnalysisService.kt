package com.grupo03.solea.data.services

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class ReceiptAnalysisResponse(
    val result: String?,
    val error: String?
)

interface ReceiptAnalysisService {
    @Multipart
    @POST("analyze-image")
    suspend fun analyzeReceipt(
        @Part file: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Response<ReceiptAnalysisResponse>
}
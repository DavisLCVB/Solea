package com.grupo03.solea.data.services

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.grupo03.solea.data.models.AuthResult
import com.grupo03.solea.data.models.User

interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult

    suspend fun signInWithGoogle(context: Context, request: GetCredentialRequest): AuthResult
    fun generateGoogleRequest(): GetCredentialRequest?
    suspend fun signOut(): AuthResult
    suspend fun getCurrentUser(): User?
}
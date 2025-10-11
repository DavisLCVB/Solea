package com.grupo03.solea.data.services.interfaces

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.AuthResult

interface AuthService {
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        displayName: String? = null,
        photoUrl: String? = null
    ): AuthResult

    suspend fun signInWithGoogle(context: Context, request: GetCredentialRequest): AuthResult
    fun generateGoogleRequest(): GetCredentialRequest?
    suspend fun signOut(): AuthResult
    suspend fun getCurrentUser(): User?
    suspend fun updateUserProfile(displayName: String?, photoUrl: String?): AuthResult
}
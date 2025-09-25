package com.grupo03.solea.data.repositories

import com.grupo03.solea.data.models.AuthResult
import com.grupo03.solea.data.models.User

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult

    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun getCurrentUser(): User?
}

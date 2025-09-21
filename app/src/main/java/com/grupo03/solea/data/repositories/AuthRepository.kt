package com.grupo03.solea.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.grupo03.solea.data.models.AuthResult
import com.grupo03.solea.data.models.User
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun getCurrentUser(): User?
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): AuthResult {
        val authResponse = auth.signInWithEmailAndPassword(email, password).await()
        val authResult = if (authResponse.user != null) {
            AuthResult(
                success = true,
                user = User(
                    uid = authResponse.user?.uid ?: "",
                    email = authResponse.user?.email ?: "",
                    displayName = authResponse.user?.displayName,
                    photoUrl = authResponse.user?.photoUrl?.toString()
                ),
                message = authResponse.toString()
            )
        } else {
            AuthResult(
                success = false,
                message = authResponse.toString()
            )
        }
        return authResult
    }

    override suspend fun signUp(email: String, password: String): AuthResult {
        val authResponse = auth.createUserWithEmailAndPassword(email, password).await()
        val authResult = if (authResponse.user != null) {
            AuthResult(
                success = true,
                user = User(
                    uid = authResponse.user?.uid ?: "",
                    email = authResponse.user?.email ?: "",
                    displayName = authResponse.user?.displayName,
                    photoUrl = authResponse.user?.photoUrl?.toString()
                ),
                message = authResponse.toString()
            )
        } else {
            AuthResult(
                success = false,
                message = authResponse.toString()
            )
        }
        return authResult
    }

    override suspend fun signOut(): AuthResult {
        return try {
            auth.signOut()
            AuthResult(success = true, message = null)
        } catch (e: Exception) {
            AuthResult(success = false, message = e.message)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            User(
                uid = currentUser.uid,
                email = currentUser.email ?: "",
                displayName = currentUser.displayName,
                photoUrl = currentUser.photoUrl?.toString()
            )
        } else {
            null
        }
    }
}
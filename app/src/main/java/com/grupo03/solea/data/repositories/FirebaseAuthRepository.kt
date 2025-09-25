package com.grupo03.solea.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.grupo03.solea.data.models.AuthResult
import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.ErrorCode
import kotlinx.coroutines.tasks.await


class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
) : AuthRepository {

    private companion object {
        const val TAG = "FirebaseAuthRepository"
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val authResponse = auth.signInWithEmailAndPassword(email, password).await()
            if (authResponse.user != null) {
                AuthResult(
                    success = true,
                    user = User(
                        uid = authResponse.user!!.uid,
                        email = authResponse.user!!.email ?: "",
                        displayName = authResponse.user!!.displayName,
                        photoUrl = authResponse.user!!.photoUrl?.toString()
                    ),
                    errorCode = null
                )
            } else {
                AuthResult(
                    success = false,
                    errorCode = ErrorCode.Auth.UNKNOWN_ERROR
                )
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthInvalidUserException -> ErrorCode.Auth.USER_NOT_FOUND
                is FirebaseAuthInvalidCredentialsException -> ErrorCode.Auth.INVALID_CREDENTIALS
                is FirebaseAuthUserCollisionException -> ErrorCode.Auth.USER_COLLISION
                is FirebaseAuthEmailException -> ErrorCode.Auth.EMAIL_ERROR
                else -> ErrorCode.Auth.UNKNOWN_ERROR
            }

            AuthResult(
                success = false,
                user = null,
                errorCode = errorMessage
            )
        }
    }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val authResponse = auth.createUserWithEmailAndPassword(email, password).await()

            if (authResponse.user != null) {
                AuthResult(
                    success = true,
                    user = User(
                        uid = authResponse.user!!.uid,
                        email = authResponse.user!!.email ?: "",
                        displayName = authResponse.user!!.displayName,
                        photoUrl = authResponse.user!!.photoUrl?.toString()
                    ),
                    errorCode = null
                )
            } else {
                AuthResult(
                    success = false,
                    errorCode = null
                )
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> ErrorCode.Auth.INVALID_CREDENTIALS
                is FirebaseAuthUserCollisionException -> ErrorCode.Auth.USER_COLLISION
                is FirebaseAuthEmailException -> ErrorCode.Auth.EMAIL_ERROR
                else -> ErrorCode.Auth.UNKNOWN_ERROR
            }

            AuthResult(
                success = false,
                errorCode = errorMessage
            )
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResponse = auth.signInWithCredential(credential).await()

            Log.d(TAG, "signInWithGoogle: ${authResponse.user?.email}")

            if (authResponse.user != null) {
                AuthResult(
                    success = true,
                    user = User(
                        uid = authResponse.user!!.uid,
                        email = authResponse.user!!.email ?: "",
                        displayName = authResponse.user!!.displayName,
                        photoUrl = authResponse.user!!.photoUrl?.toString()
                    ),
                    errorCode = null
                )
            } else {
                AuthResult(
                    success = false,
                    errorCode = ErrorCode.Auth.UNKNOWN_ERROR
                )
            }
        } catch (e: Exception) {
            val errorCode = when (e) {
                is FirebaseAuthInvalidCredentialsException -> ErrorCode.Auth.INVALID_CREDENTIALS
                is FirebaseAuthUserCollisionException -> ErrorCode.Auth.USER_COLLISION
                else -> ErrorCode.Auth.GOOGLE_SIGN_IN_FAILED
            }

            AuthResult(
                success = false,
                errorCode = errorCode
            )
        }
    }


    override suspend fun signOut(): AuthResult {
        return try {
            auth.signOut()
            AuthResult(success = true, errorCode = null)
        } catch (_: Exception) {
            AuthResult(success = false, errorCode = ErrorCode.Auth.UNKNOWN_ERROR)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser
        return currentUser?.let {
            User(
                uid = it.uid,
                email = it.email ?: "",
                displayName = it.displayName,
                photoUrl = it.photoUrl?.toString()
            )
        }
    }
}
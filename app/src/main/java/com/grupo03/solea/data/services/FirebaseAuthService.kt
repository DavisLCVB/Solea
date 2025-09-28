package com.grupo03.solea.data.services

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.grupo03.solea.data.models.AuthResult
import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.ErrorCode
import com.grupo03.solea.utils.ServiceConstants
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(
    private val auth: FirebaseAuth,
) : AuthService {

    private companion object {
        const val TAG = "FirebaseAuthRepository"
    }

    override fun generateGoogleRequest(): GetCredentialRequest? {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(ServiceConstants.WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .setNonce(null)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            return request
        } catch (e: Exception) {
            Log.d(TAG, "generateGoogleRequest: ${e.message}")
            return null
        }
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
            Log.d(TAG, "signInWithEmailAndPassword: ${e.message}")
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
            Log.d(TAG, "signUpWithEmailAndPassword: ${e.message}")
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

    override suspend fun signInWithGoogle(
        context: Context,
        request: GetCredentialRequest
    ): AuthResult {
        return try {
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential !is CustomCredential && credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return AuthResult(
                    success = false,
                    errorCode = ErrorCode.Auth.GOOGLE_SIGN_IN_FAILED
                )
            }
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResponse = auth.signInWithCredential(authCredential).await()
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
            Log.d(TAG, "signInWithGoogle: ${e.message}")
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
        } catch (e: Exception) {
            Log.d(TAG, "signOut: ${e.message}")
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
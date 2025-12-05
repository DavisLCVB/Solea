package com.grupo03.solea.data.services.firebase

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
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
import com.google.firebase.auth.userProfileChangeRequest
import com.grupo03.solea.data.models.User
import com.grupo03.solea.data.repositories.interfaces.UserRepository
import com.grupo03.solea.data.services.interfaces.AuthService
import com.grupo03.solea.utils.AuthResult
import com.grupo03.solea.utils.AuthError
import com.grupo03.solea.utils.RepositoryResult
import com.grupo03.solea.utils.ServiceConstants
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
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
                val uid = authResponse.user!!.uid

                // Get full user profile from Firestore (includes currency)
                val userProfileResult = userRepository.getUserProfile(uid)
                val user = when (userProfileResult) {
                    is RepositoryResult.Success -> userProfileResult.data
                    is RepositoryResult.Error -> {
                        // Fallback to auth data if Firestore profile not found
                        User(
                            uid = uid,
                            email = authResponse.user!!.email ?: "",
                            displayName = authResponse.user!!.displayName,
                            photoUrl = authResponse.user!!.photoUrl?.toString()
                        )
                    }
                }

                AuthResult.Success(user = user)
            } else {
                AuthResult.Error(AuthError.UNKNOWN_ERROR)
            }
        } catch (e: Exception) {
            Log.d(TAG, "signInWithEmailAndPassword: ${e.message}")
            val error = when (e) {
                is FirebaseAuthInvalidUserException -> AuthError.USER_NOT_FOUND
                is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
                is FirebaseAuthUserCollisionException -> AuthError.USER_COLLISION
                is FirebaseAuthEmailException -> AuthError.EMAIL_ERROR
                else -> AuthError.UNKNOWN_ERROR
            }

            AuthResult.Error(error)
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        displayName: String?,
        photoUrl: String?
    ): AuthResult {
        return try {
            val authResponse = auth.createUserWithEmailAndPassword(email, password).await()

            if (authResponse.user != null) {
                // Update profile if displayName or photoUrl provided
                if (displayName != null || photoUrl != null) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName?.let { this.displayName = it }
                        photoUrl?.let { this.photoUri = it.toUri() }
                    }
                    authResponse.user!!.updateProfile(profileUpdates).await()
                }

                // Reload user to get updated profile
                authResponse.user!!.reload().await()

                val uid = authResponse.user!!.uid

                // Try to get full user profile from Firestore (includes currency)
                // Note: This might not exist yet if called before profile creation
                val userProfileResult = userRepository.getUserProfile(uid)
                val user = when (userProfileResult) {
                    is RepositoryResult.Success -> userProfileResult.data
                    is RepositoryResult.Error -> {
                        // Fallback to auth data if Firestore profile not created yet
                        User(
                            uid = uid,
                            email = authResponse.user!!.email ?: "",
                            displayName = authResponse.user!!.displayName,
                            photoUrl = authResponse.user!!.photoUrl?.toString()
                        )
                    }
                }

                AuthResult.Success(user = user)
            } else {
                AuthResult.Error(AuthError.UNKNOWN_ERROR)
            }
        } catch (e: Exception) {
            Log.d(TAG, "signUpWithEmailAndPassword: ${e.message}")
            val error = when (e) {
                is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
                is FirebaseAuthUserCollisionException -> AuthError.USER_COLLISION
                is FirebaseAuthEmailException -> AuthError.EMAIL_ERROR
                else -> AuthError.UNKNOWN_ERROR
            }

            AuthResult.Error(error)
        }
    }

    override suspend fun signInWithGoogle(
        context: Context,
        request: GetCredentialRequest
    ): AuthResult {
        return try {
            val credentialManager = CredentialManager.Companion.create(context)
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential !is CustomCredential && credential.type != GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return AuthResult.Error(AuthError.GOOGLE_SIGN_IN_FAILED)
            }
            val googleIdTokenCredential =
                GoogleIdTokenCredential.Companion.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResponse = auth.signInWithCredential(authCredential).await()
            if (authResponse.user != null) {
                val uid = authResponse.user!!.uid

                // Get full user profile from Firestore (includes currency)
                val userProfileResult = userRepository.getUserProfile(uid)
                val user = when (userProfileResult) {
                    is RepositoryResult.Success -> userProfileResult.data
                    is RepositoryResult.Error -> {
                        // Fallback to auth data if Firestore profile not found
                        User(
                            uid = uid,
                            email = authResponse.user!!.email ?: "",
                            displayName = authResponse.user!!.displayName,
                            photoUrl = authResponse.user!!.photoUrl?.toString()
                        )
                    }
                }

                AuthResult.Success(user = user)
            } else {
                AuthResult.Error(AuthError.UNKNOWN_ERROR)
            }
        } catch (e: Exception) {
            Log.d(TAG, "signInWithGoogle: ${e.message}")
            val error = when (e) {
                is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
                is FirebaseAuthUserCollisionException -> AuthError.USER_COLLISION
                else -> AuthError.GOOGLE_SIGN_IN_FAILED
            }

            AuthResult.Error(error)
        }
    }


    override suspend fun signOut(): AuthResult {
        return try {
            auth.signOut()
            // Sign out doesn't return a user, so we create a dummy user
            AuthResult.Success(
                user = User(uid = "", email = "", displayName = null, photoUrl = null)
            )
        } catch (e: Exception) {
            Log.d(TAG, "signOut: ${e.message}")
            AuthResult.Error(AuthError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null

        // Try to get full user profile from Firestore (includes currency)
        val userProfileResult = userRepository.getUserProfile(currentUser.uid)
        return when (userProfileResult) {
            is RepositoryResult.Success -> userProfileResult.data
            is RepositoryResult.Error -> {
                // Fallback to auth data if Firestore profile not found
                User(
                    uid = currentUser.uid,
                    email = currentUser.email ?: "",
                    displayName = currentUser.displayName,
                    photoUrl = currentUser.photoUrl?.toString()
                )
            }
        }
    }

    override suspend fun updateUserProfile(displayName: String?, photoUrl: String?): AuthResult {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return AuthResult.Error(AuthError.USER_NOT_FOUND)
            }

            val profileUpdates = userProfileChangeRequest {
                displayName?.let { this.displayName = it }
                photoUrl?.let { this.photoUri = it.toUri() }
            }

            currentUser.updateProfile(profileUpdates).await()
            currentUser.reload().await()

            AuthResult.Success(
                user = User(
                    uid = currentUser.uid,
                    email = currentUser.email ?: "",
                    displayName = currentUser.displayName,
                    photoUrl = currentUser.photoUrl?.toString()
                )
            )
        } catch (e: Exception) {
            Log.d(TAG, "updateUserProfile: ${e.message}")
            AuthResult.Error(AuthError.UNKNOWN_ERROR)
        }
    }
}
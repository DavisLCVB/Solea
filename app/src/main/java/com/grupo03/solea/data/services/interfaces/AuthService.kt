package com.grupo03.solea.data.services.interfaces

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.AuthResult

/**
 * Service interface for user authentication and account management.
 *
 * This service handles authentication via Firebase, supporting both email/password
 * and Google OAuth sign-in methods.
 */
interface AuthService {

    /**
     * Signs in a user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return AuthResult containing the user data on success or error information
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult

    /**
     * Creates a new user account with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @param displayName Optional display name for the user
     * @param photoUrl Optional URL to user's profile photo
     * @return AuthResult containing the user data on success or error information
     */
    suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        displayName: String? = null,
        photoUrl: String? = null
    ): AuthResult

    /**
     * Signs in a user with Google OAuth.
     *
     * @param context Android context required for credential management
     * @param request Credential request configured for Google sign-in
     * @return AuthResult containing the user data on success or error information
     */
    suspend fun signInWithGoogle(context: Context, request: GetCredentialRequest): AuthResult

    /**
     * Generates a credential request for Google sign-in.
     *
     * This should be called to prepare the request before calling signInWithGoogle.
     *
     * @return GetCredentialRequest configured for Google OAuth, or null if configuration fails
     */
    fun generateGoogleRequest(): GetCredentialRequest?

    /**
     * Signs out the current user.
     *
     * @return AuthResult indicating success or error
     */
    suspend fun signOut(): AuthResult

    /**
     * Retrieves the currently authenticated user.
     *
     * @return User object if authenticated, null if no user is signed in
     */
    suspend fun getCurrentUser(): User?

    /**
     * Updates the current user's profile information.
     *
     * @param displayName New display name (null to keep unchanged)
     * @param photoUrl New photo URL (null to keep unchanged)
     * @return AuthResult indicating success or error
     */
    suspend fun updateUserProfile(displayName: String?, photoUrl: String?): AuthResult
}

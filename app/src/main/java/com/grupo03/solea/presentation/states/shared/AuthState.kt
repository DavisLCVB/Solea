package com.grupo03.solea.presentation.states.shared

import com.grupo03.solea.data.models.User
import com.grupo03.solea.utils.AuthError

/**
 * Form type for authentication operations.
 */
enum class FormType {
    /** Sign in form */
    SIGN_IN,

    /** Sign up (registration) form */
    SIGN_UP
}

/**
 * UI state for authentication.
 *
 * This state represents the overall authentication status of the application,
 * tracking the current user and any authentication errors.
 *
 * @property user The currently authenticated user, null if not authenticated
 * @property errorCode Authentication error if an error occurred, null otherwise
 */
data class AuthState(
    val user: User? = null,
    val errorCode: AuthError? = null,
)

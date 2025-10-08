package com.grupo03.solea.utils

import com.grupo03.solea.data.models.User

/**
 * Result wrapper for authentication operations.
 *
 * This sealed class encapsulates the result of authentication operations,
 * providing type-safe handling of success and error cases.
 *
 * Use pattern matching to handle results:
 * ```kotlin
 * when (result) {
 *     is AuthResult.Success -> { /* use result.user */ }
 *     is AuthResult.Error -> { /* handle result.error */ }
 * }
 * ```
 *
 * Or use the functional extensions:
 * ```kotlin
 * result
 *     .onSuccess { user, token -> /* handle success */ }
 *     .onError { error -> /* handle error */ }
 * ```
 */
sealed class AuthResult {
    /**
     * Represents a successful authentication operation.
     *
     * @property user The authenticated user
     * @property token Optional authentication token (may be used for session management)
     */
    data class Success(
        val user: User,
        val token: String? = null
    ) : AuthResult()

    /**
     * Represents a failed authentication operation.
     *
     * @property error The specific authentication error that occurred
     */
    data class Error(
        val error: AuthError
    ) : AuthResult()

    /**
     * Executes the given action if this is a Success.
     *
     * @param action The action to execute with the user and optional token
     * @return This AuthResult for chaining
     */
    inline fun onSuccess(action: (User, String?) -> Unit): AuthResult {
        if (this is Success) action(user, token)
        return this
    }

    /**
     * Executes the given action if this is an Error.
     *
     * @param action The action to execute with the error
     * @return This AuthResult for chaining
     */
    inline fun onError(action: (AuthError) -> Unit): AuthResult {
        if (this is Error) action(error)
        return this
    }
}

/**
 * Backward compatibility function for creating AuthResult instances.
 *
 * @deprecated Use AuthResult.Success or AuthResult.Error constructors instead
 */
@Deprecated("Use AuthResult.Success or AuthResult.Error instead")
fun AuthResult(
    success: Boolean,
    user: User? = null,
    errorCode: AuthError? = null,
    token: String? = null
): AuthResult {
    return if (success && user != null) {
        AuthResult.Success(user, token)
    } else {
        AuthResult.Error(errorCode ?: AuthError.UNKNOWN_ERROR)
    }
}

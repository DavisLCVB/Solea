package com.grupo03.solea.utils

import com.grupo03.solea.data.models.User

/**
 * Result wrapper for authentication operations
 */
sealed class AuthResult {
    data class Success(
        val user: User,
        val token: String? = null
    ) : AuthResult()

    data class Error(
        val error: AuthError
    ) : AuthResult()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    fun getOrNull(): User? = when (this) {
        is Success -> user
        is Error -> null
    }

    fun errorOrNull(): AuthError? = when (this) {
        is Success -> null
        is Error -> error
    }

    inline fun onSuccess(action: (User, String?) -> Unit): AuthResult {
        if (this is Success) action(user, token)
        return this
    }

    inline fun onError(action: (AuthError) -> Unit): AuthResult {
        if (this is Error) action(error)
        return this
    }
}

// Backward compatibility extension
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
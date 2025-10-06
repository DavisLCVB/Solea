package com.grupo03.solea.utils

object Validation {
    fun checkEmail(email: String): AuthError? {
        if (email.isEmpty()) {
            return AuthError.EMAIL_EMPTY
        }
        if (email.length > ValidationConstants.MAX_EMAIL_LENGTH) {
            return AuthError.EMAIL_TOO_LONG
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthError.EMAIL_INVALID
        }
        return null
    }

    fun checkPassword(password: String): AuthError? {
        if (password.isEmpty()) {
            return AuthError.PASSWORD_EMPTY
        }
        if (password.length < ValidationConstants.MIN_PASSWORD_LENGTH) {
            return AuthError.WEAK_PASSWORD
        }
        if (password.length > ValidationConstants.MAX_PASSWORD_LENGTH) {
            return AuthError.WEAK_PASSWORD
        }
        return null
    }

    fun checkName(name: String): AuthError? {
        if (!Regex(ValidationConstants.NAME_REGEX).matches(name)) {
            return AuthError.USERNAME_INVALID
        }
        return null
    }
}
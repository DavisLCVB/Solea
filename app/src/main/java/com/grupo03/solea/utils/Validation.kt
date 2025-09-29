package com.grupo03.solea.utils

object Validation {
    fun checkEmail(email: String): ErrorCode.Auth? {
        if (email.isEmpty()) {
            return ErrorCode.Auth.EMAIL_EMPTY
        }
        if (email.length > ValidationConstants.MAX_EMAIL_LENGTH) {
            return ErrorCode.Auth.EMAIL_TOO_LONG
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ErrorCode.Auth.EMAIL_INVALID
        }
        return null
    }

    fun checkPassword(password: String): ErrorCode.Auth? {
        if (password.isEmpty()) {
            return ErrorCode.Auth.PASSWORD_EMPTY
        }
        if (password.length < ValidationConstants.MIN_PASSWORD_LENGTH) {
            return ErrorCode.Auth.WEAK_PASSWORD
        }
        if (password.length > ValidationConstants.MAX_PASSWORD_LENGTH) {
            return ErrorCode.Auth.WEAK_PASSWORD
        }
        return null
    }

    fun checkName(name: String): ErrorCode.Auth? {
        if (!Regex(ValidationConstants.NAME_REGEX).matches(name)) {
            return ErrorCode.Auth.USERNAME_INVALID
        }
        return null
    }
}
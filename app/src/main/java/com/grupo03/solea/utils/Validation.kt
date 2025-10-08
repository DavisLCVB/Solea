package com.grupo03.solea.utils

/**
 * Utility object for input validation.
 *
 * Provides validation functions for common form inputs such as email,
 * password, and user names. Returns specific error codes for different
 * validation failures.
 */
object Validation {
    /**
     * Validates an email address.
     *
     * Checks for:
     * - Non-empty email
     * - Maximum length
     * - Valid email format (using Android's email pattern)
     *
     * @param email The email address to validate
     * @return AuthError if validation fails, null if email is valid
     */
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

    /**
     * Validates a password.
     *
     * Checks for:
     * - Non-empty password
     * - Minimum length (6 characters)
     * - Maximum length (20 characters)
     *
     * @param password The password to validate
     * @return AuthError if validation fails, null if password is valid
     */
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

    /**
     * Validates a user's display name.
     *
     * Checks that the name matches the required pattern:
     * - Letters (including accented characters)
     * - Numbers
     * - Spaces and periods
     * - Length between 3 and 40 characters
     *
     * @param name The name to validate
     * @return AuthError if validation fails, null if name is valid
     */
    fun checkName(name: String): AuthError? {
        if (!Regex(ValidationConstants.NAME_REGEX).matches(name)) {
            return AuthError.USERNAME_INVALID
        }
        return null
    }
}

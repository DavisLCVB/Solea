package com.grupo03.solea.presentation.states.screens

/**
 * UI state for the sign-in form.
 *
 * Tracks form field values, validation status, and loading state for user authentication.
 *
 * @property email User's email address
 * @property password User's password
 * @property isEmailValid Whether the email format is valid
 * @property isLoading Whether an authentication operation is in progress
 */
data class SignInFormState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = true,
    val isLoading: Boolean = false,
)

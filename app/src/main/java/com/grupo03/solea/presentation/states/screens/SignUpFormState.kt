package com.grupo03.solea.presentation.states.screens

/**
 * UI state for the sign-up (registration) form.
 *
 * Tracks all registration form fields, their validation status, and loading state.
 *
 * @property name User's display name
 * @property email User's email address
 * @property password User's chosen password
 * @property confirmPassword Password confirmation for validation
 * @property photoUri Optional URI to user's profile photo
 * @property isNameValid Whether the name meets validation requirements
 * @property isEmailValid Whether the email format is valid
 * @property isPasswordValid Whether the password meets strength requirements
 * @property isLoading Whether a registration operation is in progress
 */
data class SignUpFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val photoUri: String? = null,
    val isNameValid: Boolean = true,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isLoading: Boolean = false,
)

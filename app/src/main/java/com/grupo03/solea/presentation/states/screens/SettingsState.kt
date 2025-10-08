package com.grupo03.solea.presentation.states.screens

/**
 * UI state for the settings screen.
 *
 * Tracks all user preferences and settings, including theme, language,
 * and notification preferences.
 *
 * @property notificationsEnabled Whether push notifications are enabled
 * @property isDarkTheme Whether dark theme is active
 * @property selectedLanguage Current language code ("es" for Spanish, "en" for English)
 * @property isLoading Whether settings are being saved/loaded
 * @property error Error message if settings operation failed, null otherwise
 */
data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val isDarkTheme: Boolean = false,
    val selectedLanguage: String = "es", // "es" o "en"
    val isLoading: Boolean = false,
    val error: String? = null
)

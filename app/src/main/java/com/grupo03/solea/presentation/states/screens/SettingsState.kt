package com.grupo03.solea.presentation.states.screens

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val isDarkTheme: Boolean = false,
    val selectedLanguage: String = "es", // "es" o "en"
    val isLoading: Boolean = false,
    val error: String? = null
)

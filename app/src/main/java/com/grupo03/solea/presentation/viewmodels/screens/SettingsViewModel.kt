package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.repositories.interfaces.UserPreferencesRepository
import com.grupo03.solea.presentation.states.screens.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen.
 *
 * Manages user preferences including notifications, theme (dark/light), and language settings.
 * Automatically loads current preferences from DataStore on initialization and provides
 * methods to update individual preferences.
 *
 * @property userPreferencesRepository Repository for persisting user preferences
 */
class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    /** Settings state including notifications, theme, and language preferences */
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    /**
     * Loads user preferences from DataStore.
     *
     * Combines all preference flows (notifications, theme, language) and updates
     * the UI state reactively whenever any preference changes.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.getNotificationsEnabled(),
                userPreferencesRepository.getDarkTheme(),
                userPreferencesRepository.getLanguage()
            ) { notifications, darkTheme, language ->
                SettingsState(
                    notificationsEnabled = notifications,
                    isDarkTheme = darkTheme,
                    selectedLanguage = language
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Toggles notification preferences.
     *
     * @param enabled Whether notifications should be enabled
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.saveNotificationsEnabled(enabled)
                _uiState.value = _uiState.value.copy(
                    notificationsEnabled = enabled,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al guardar preferencia de notificaciones"
                )
            }
        }
    }

    /**
     * Toggles theme preference between dark and light mode.
     *
     * @param isDark Whether dark theme should be enabled
     */
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.saveDarkTheme(isDark)
                _uiState.value = _uiState.value.copy(
                    isDarkTheme = isDark,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al guardar preferencia de tema"
                )
            }
        }
    }

    /**
     * Changes the application language preference.
     *
     * @param languageCode Language code (e.g., "en", "es")
     */
    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.saveLanguage(languageCode)
                _uiState.value = _uiState.value.copy(
                    selectedLanguage = languageCode,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al guardar preferencia de idioma"
                )
            }
        }
    }

    /**
     * Clears any error message from the settings state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

package com.grupo03.solea.data.repositories.interfaces

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user preferences stored locally.
 *
 * This repository handles application settings and preferences that are
 * stored on the device using DataStore.
 */
interface UserPreferencesRepository {

    /**
     * Saves the notifications enabled preference.
     *
     * @param enabled True to enable notifications, false to disable
     */
    suspend fun saveNotificationsEnabled(enabled: Boolean)

    /**
     * Observes the notifications enabled preference.
     *
     * @return Flow emitting the current notifications enabled state
     */
    fun getNotificationsEnabled(): Flow<Boolean>

    /**
     * Saves the dark theme preference.
     *
     * @param isDark True to enable dark theme, false for light theme
     */
    suspend fun saveDarkTheme(isDark: Boolean)

    /**
     * Observes the dark theme preference.
     *
     * @return Flow emitting the current dark theme state
     */
    fun getDarkTheme(): Flow<Boolean>

    /**
     * Saves the language preference.
     *
     * @param languageCode Language code (e.g., "en", "es", "fr")
     */
    suspend fun saveLanguage(languageCode: String)

    /**
     * Observes the language preference.
     *
     * @return Flow emitting the current language code
     */
    fun getLanguage(): Flow<String>

    /**
     * Saves the preferred currency.
     *
     * @param currencyCode Currency code (e.g., "USD", "EUR", "ARS")
     */
    suspend fun saveCurrency(currencyCode: String)

    /**
     * Observes the currency preference.
     *
     * @return Flow emitting the current currency code, or null if not set (should use device detection)
     */
    fun getCurrency(): Flow<String?>

    /**
     * Clears the currency preference to use auto-detection.
     */
    suspend fun clearCurrency()

    /**
     * Clears all stored preferences.
     *
     * This resets all settings to their default values.
     */
    suspend fun clearAllPreferences()
}

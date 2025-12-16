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
     * Saves the last movements cache refresh timestamp.
     *
     * @param timestamp Timestamp in milliseconds
     */
    suspend fun saveLastMovementsRefresh(timestamp: Long)

    /**
     * Observes the last movements cache refresh timestamp.
     *
     * @return Flow emitting the timestamp in milliseconds
     */
    fun getLastMovementsRefresh(): Flow<Long>

    /**
     * Clears all stored preferences.
     *
     * This resets all settings to their default values.
     */
    suspend fun clearAllPreferences()

    /**
     * Quick-start preference: whether to open a quick action on app start
     */
    suspend fun saveQuickStartEnabled(enabled: Boolean)
    fun getQuickStartEnabled(): Flow<Boolean>

    /**
     * Quick-start target: "receipt" | "voice"
     */
    suspend fun saveQuickStartTarget(target: String)
    fun getQuickStartTarget(): Flow<String>
}

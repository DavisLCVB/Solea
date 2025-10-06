package com.grupo03.solea.data.repositories.interfaces

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    suspend fun saveNotificationsEnabled(enabled: Boolean)
    fun getNotificationsEnabled(): Flow<Boolean>

    suspend fun saveDarkTheme(isDark: Boolean)
    fun getDarkTheme(): Flow<Boolean>

    suspend fun saveLanguage(languageCode: String)
    fun getLanguage(): Flow<String>

    suspend fun clearAllPreferences()
}

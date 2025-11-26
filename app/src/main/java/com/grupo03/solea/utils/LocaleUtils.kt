package com.grupo03.solea.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Utility object for locale and language management.
 *
 * Provides functions for:
 * - Wrapping contexts with specific locales for instant language switching
 * - Getting list of supported languages
 * - Managing language preferences
 *
 * This enables instant language switching without requiring app restart by creating
 * locale-specific contexts that can be provided through CompositionLocal in Compose.
 */
object LocaleUtils {
    /**
     * Wraps a context with a specific locale configuration.
     *
     * This is the core mechanism that enables instant language switching. By creating
     * a new context with updated Configuration, we can change the locale without
     * requiring Activity recreation.
     *
     * @param context The base context to wrap
     * @param languageCode ISO 639-1 language code (e.g., "en", "es")
     * @return A new context with the specified locale applied
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    /**
     * Gets the list of supported languages in the app.
     *
     * @return List of LanguageOption containing code, native name, and emoji flag
     */
    fun getSupportedLanguages(): List<LanguageOption> = listOf(
        LanguageOption(
            code = "en",
            nativeName = "English",
            emoji = "\uD83C\uDDFA\uD83C\uDDF8"  // 🇺🇸
        ),
        LanguageOption(
            code = "es",
            nativeName = "Español",
            emoji = "\uD83C\uDDEA\uD83C\uDDF8"  // 🇪🇸
        )
    )

    /**
     * Gets the display name of a language in its native form.
     *
     * @param languageCode ISO 639-1 language code
     * @return Native display name, or the code itself if not found
     */
    fun getLanguageName(languageCode: String): String {
        return getSupportedLanguages()
            .find { it.code == languageCode }
            ?.nativeName
            ?: languageCode
    }

    /**
     * Checks if a language code is supported by the app.
     *
     * @param languageCode ISO 639-1 language code to check
     * @return True if the language is supported, false otherwise
     */
    fun isLanguageSupported(languageCode: String): Boolean {
        return getSupportedLanguages().any { it.code == languageCode }
    }
}

/**
 * Data class representing a language option for selection.
 *
 * @property code ISO 639-1 language code (e.g., "en", "es")
 * @property nativeName Language name in its native form (e.g., "English", "Español")
 * @property emoji Unicode emoji flag representing the language/country
 */
data class LanguageOption(
    val code: String,
    val nativeName: String,
    val emoji: String
)

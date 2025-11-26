package com.grupo03.solea.utils

import java.util.Currency
import java.util.Locale

/**
 * Utility object for currency handling and conversion.
 *
 * Provides functions for:
 * - Detecting device currency based on locale
 * - Getting currency symbols
 * - Formatting amounts with currency
 * - Converting between currencies (using static exchange rates)
 *
 * Note: Exchange rates are static and should be updated periodically.
 * For production use, consider integrating a real-time currency API.
 */
object CurrencyUtils {
    /**
     * Gets the currency code based on the device's locale.
     *
     * @return Currency code (e.g., "USD", "EUR", "ARS"). Defaults to "USD" if unavailable.
     */
    fun getDeviceCurrency(): String {
        return try {
            val locale = Locale.getDefault()
            val currency = Currency.getInstance(locale)
            currency.currencyCode
        } catch (_: Exception) {
            "USD"
        }
    }

    /**
     * Gets the currency symbol based on the device's locale.
     *
     * Now uses getCurrencyByCountry() to ensure consistency between
     * currency code and symbol detection.
     *
     * @return Currency symbol (e.g., "$", "€", "AR$"). Defaults to "$" if unavailable.
     */
    fun getDeviceCurrencySymbol(): String {
        val currencyCode = getCurrencyByCountry()
        return getCurrencySymbol(currencyCode)
    }

    /**
     * Mapping of country codes to currency codes for common Latin American and European countries.
     */
    private val countryToCurrency = mapOf(
        "AR" to "ARS", // Argentina
        "BO" to "BOB", // Bolivia
        "BR" to "BRL", // Brazil
        "CL" to "CLP", // Chile
        "CO" to "COP", // Colombia
        "CR" to "CRC", // Costa Rica
        "CU" to "CUP", // Cuba
        "DO" to "DOP", // Dominican Republic
        "EC" to "USD", // Ecuador (uses USD)
        "SV" to "USD", // El Salvador (uses USD)
        "GT" to "GTQ", // Guatemala
        "HN" to "HNL", // Honduras
        "MX" to "MXN", // Mexico
        "NI" to "NIO", // Nicaragua
        "PA" to "PAB", // Panama
        "PY" to "PYG", // Paraguay
        "PE" to "PEN", // Peru
        "UY" to "UYU", // Uruguay
        "VE" to "VES", // Venezuela
        "ES" to "EUR", // Spain
        "US" to "USD", // United States
    )

    /**
     * Gets the currency based on the country code from the device's locale.
     *
     * Uses a predefined mapping for common countries, falls back to device currency if not mapped.
     *
     * @return Currency code (e.g., "USD", "EUR"). Defaults to "USD" if unavailable.
     */
    fun getCurrencyByCountry(): String {
        return try {
            val locale = Locale.getDefault()
            val countryCode = locale.country
            countryToCurrency[countryCode] ?: getDeviceCurrency()
        } catch (_: Exception) {
            "USD"
        }
    }

    /**
     * Gets the symbol for a specific currency code.
     *
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
     * @return The currency symbol. Returns the currency code itself if symbol is unavailable.
     */
    fun getCurrencySymbol(currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            currency.symbol
        } catch (_: Exception) {
            currencyCode
        }
    }

    /**
     * Static exchange rates relative to USD.
     *
     * Note: These are approximate rates and should be updated periodically.
     * For production applications, use a real-time currency exchange rate API.
     */
    private val exchangeRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "ARS" to 1000.0,
        "BOB" to 6.91,
        "BRL" to 5.0,
        "CLP" to 950.0,
        "COP" to 4000.0,
        "CRC" to 520.0,
        "CUP" to 24.0,
        "DOP" to 59.0,
        "GTQ" to 7.8,
        "HNL" to 24.7,
        "MXN" to 17.0,
        "NIO" to 36.5,
        "PAB" to 1.0,
        "PYG" to 7300.0,
        "PEN" to 3.7,
        "UYU" to 39.0,
        "VES" to 36.0
    )

    /**
     * Converts an amount from one currency to another using static exchange rates.
     *
     * The conversion uses USD as an intermediary:
     * 1. Convert from source currency to USD
     * 2. Convert from USD to target currency
     *
     * @param amount The amount to convert
     * @param fromCurrency The source currency code
     * @param toCurrency The target currency code
     * @return The converted amount. Returns the original amount if conversion is not possible.
     */
    fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount

        val fromRate = exchangeRates[fromCurrency] ?: return amount
        val toRate = exchangeRates[toCurrency] ?: return amount

        // Convert to USD first, then to target currency
        val amountInUSD = amount / fromRate
        return amountInUSD * toRate
    }

    /**
     * Checks if conversion is possible between two currencies.
     *
     * @param fromCurrency The source currency code
     * @param toCurrency The target currency code
     * @return True if both currencies are supported in the exchange rates table, false otherwise
     */
    fun canConvert(fromCurrency: String, toCurrency: String): Boolean {
        return exchangeRates.containsKey(fromCurrency) && exchangeRates.containsKey(toCurrency)
    }

    /**
     * Data class representing a currency option for selection.
     *
     * @property code ISO 4217 currency code (e.g., "USD", "EUR")
     * @property symbol Currency symbol (e.g., "$", "€")
     * @property name Human-readable name (e.g., "US Dollar", "Euro")
     */
    data class CurrencyOption(
        val code: String,
        val symbol: String,
        val name: String
    )

    /**
     * Returns a list of all supported currencies for user selection.
     *
     * Includes all currencies from the exchange rates table with their
     * symbols and localized names.
     *
     * @return List of currency options sorted by name
     */
    fun getSupportedCurrencies(): List<CurrencyOption> {
        val currencies = listOf(
            CurrencyOption("USD", getCurrencySymbol("USD"), "Dólar estadounidense"),
            CurrencyOption("EUR", getCurrencySymbol("EUR"), "Euro"),
            CurrencyOption("ARS", getCurrencySymbol("ARS"), "Peso argentino"),
            CurrencyOption("MXN", getCurrencySymbol("MXN"), "Peso mexicano"),
            CurrencyOption("CLP", getCurrencySymbol("CLP"), "Peso chileno"),
            CurrencyOption("COP", getCurrencySymbol("COP"), "Peso colombiano"),
            CurrencyOption("BRL", getCurrencySymbol("BRL"), "Real brasileño"),
            CurrencyOption("PEN", getCurrencySymbol("PEN"), "Sol peruano"),
            CurrencyOption("UYU", getCurrencySymbol("UYU"), "Peso uruguayo"),
            CurrencyOption("PYG", getCurrencySymbol("PYG"), "Guaraní paraguayo"),
            CurrencyOption("BOB", getCurrencySymbol("BOB"), "Boliviano"),
            CurrencyOption("VES", getCurrencySymbol("VES"), "Bolívar venezolano"),
            CurrencyOption("CRC", getCurrencySymbol("CRC"), "Colón costarricense"),
            CurrencyOption("GTQ", getCurrencySymbol("GTQ"), "Quetzal guatemalteco"),
            CurrencyOption("HNL", getCurrencySymbol("HNL"), "Lempira hondureño"),
            CurrencyOption("NIO", getCurrencySymbol("NIO"), "Córdoba nicaragüense"),
            CurrencyOption("PAB", getCurrencySymbol("PAB"), "Balboa panameño"),
            CurrencyOption("DOP", getCurrencySymbol("DOP"), "Peso dominicano"),
            CurrencyOption("CUP", getCurrencySymbol("CUP"), "Peso cubano")
        )
        return currencies.sortedBy { it.name }
    }

    /**
     * Gets the currency to use considering user preference.
     *
     * If userPreference is provided and valid, uses that. Otherwise
     * falls back to device detection.
     *
     * @param userPreference User's saved currency preference, null if not set
     * @return Currency code to use
     */
    fun getCurrency(userPreference: String?): String {
        return if (userPreference != null && exchangeRates.containsKey(userPreference)) {
            userPreference
        } else {
            getCurrencyByCountry()
        }
    }

    /**
     * Gets the currency symbol considering user preference.
     *
     * If userPreference is provided and valid, uses that. Otherwise
     * falls back to device detection.
     *
     * @param userPreference User's saved currency preference, null if not set
     * @return Currency symbol to use
     */
    fun getCurrencySymbolWithPreference(userPreference: String?): String {
        val currencyCode = getCurrency(userPreference)
        return getCurrencySymbol(currencyCode)
    }
}

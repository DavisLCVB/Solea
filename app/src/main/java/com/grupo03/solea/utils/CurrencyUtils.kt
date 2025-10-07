package com.grupo03.solea.utils

import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    /**
     * Obtiene el código de moneda basado en el Locale del dispositivo
     */
    fun getDeviceCurrency(): String {
        return try {
            val locale = Locale.getDefault()
            val currency = Currency.getInstance(locale)
            currency.currencyCode
        } catch (e: Exception) {
            // Si no se puede obtener la moneda, usar USD por defecto
            "USD"
        }
    }

    /**
     * Obtiene el símbolo de moneda basado en el Locale del dispositivo
     */
    fun getDeviceCurrencySymbol(): String {
        return try {
            val locale = Locale.getDefault()
            val currency = Currency.getInstance(locale)
            currency.symbol
        } catch (e: Exception) {
            "$"
        }
    }

    /**
     * Mapeo de códigos de país a códigos de moneda comunes
     */
    private val countryToCurrency = mapOf(
        "AR" to "ARS", // Argentina
        "BO" to "BOB", // Bolivia
        "BR" to "BRL", // Brasil
        "CL" to "CLP", // Chile
        "CO" to "COP", // Colombia
        "CR" to "CRC", // Costa Rica
        "CU" to "CUP", // Cuba
        "DO" to "DOP", // República Dominicana
        "EC" to "USD", // Ecuador (usa USD)
        "SV" to "USD", // El Salvador (usa USD)
        "GT" to "GTQ", // Guatemala
        "HN" to "HNL", // Honduras
        "MX" to "MXN", // México
        "NI" to "NIO", // Nicaragua
        "PA" to "PAB", // Panamá
        "PY" to "PYG", // Paraguay
        "PE" to "PEN", // Perú
        "UY" to "UYU", // Uruguay
        "VE" to "VES", // Venezuela
        "ES" to "EUR", // España
        "US" to "USD", // Estados Unidos
    )

    /**
     * Obtiene la moneda basándose en el código de país del Locale
     */
    fun getCurrencyByCountry(): String {
        return try {
            val locale = Locale.getDefault()
            val countryCode = locale.country
            countryToCurrency[countryCode] ?: getDeviceCurrency()
        } catch (e: Exception) {
            "USD"
        }
    }

    /**
     * Obtiene el símbolo de una moneda específica
     */
    fun getCurrencySymbol(currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            currency.symbol
        } catch (e: Exception) {
            currencyCode
        }
    }

    /**
     * Formatea un monto con su moneda
     */
    fun formatAmount(amount: Double, currencyCode: String): String {
        val symbol = getCurrencySymbol(currencyCode)
        return "$symbol ${String.format(Locale.getDefault(), "%.2f", amount)}"
    }

    /**
     * Tasas de cambio aproximadas respecto al USD (actualizar periódicamente)
     * Para una solución en producción, usar una API de tasas de cambio
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
     * Convierte un monto de una moneda a otra
     * @param amount Monto a convertir
     * @param fromCurrency Moneda origen
     * @param toCurrency Moneda destino
     * @return Monto convertido, o el monto original si no se puede convertir
     */
    fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount

        val fromRate = exchangeRates[fromCurrency] ?: return amount
        val toRate = exchangeRates[toCurrency] ?: return amount

        // Convertir a USD primero, luego a la moneda destino
        val amountInUSD = amount / fromRate
        return amountInUSD * toRate
    }

    /**
     * Verifica si se puede convertir entre dos monedas
     */
    fun canConvert(fromCurrency: String, toCurrency: String): Boolean {
        return exchangeRates.containsKey(fromCurrency) && exchangeRates.containsKey(toCurrency)
    }
}

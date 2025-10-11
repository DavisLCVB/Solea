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
}

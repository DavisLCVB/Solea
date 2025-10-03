package com.grupo03.solea.utils

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException

object JsonParsingUtils {
    
    fun getParsingErrorMessage(exception: Exception, rawJson: String): String {
        return when (exception) {
            is JsonSyntaxException -> {
                when {
                    rawJson.isEmpty() -> "La respuesta está vacía"
                    !rawJson.trim().startsWith("{") && !rawJson.trim().startsWith("[") -> 
                        "La respuesta no es JSON válido (no empieza con { o [)"
                    rawJson.contains("error") && rawJson.contains("message") -> 
                        "El servidor devolvió un error en lugar de datos de boleta"
                    else -> "El formato JSON de la respuesta no es válido: ${exception.message}"
                }
            }
            is MalformedJsonException -> "El JSON está mal formado: ${exception.message}"
            is IllegalStateException -> {
                when {
                    exception.message?.contains("Expected") == true -> 
                        "Tipo de dato incorrecto en JSON: ${exception.message}"
                    else -> "Estado inesperado durante el parsing: ${exception.message}"
                }
            }
            else -> "Error inesperado parseando JSON: ${exception.message}"
        }
    }
    
    fun logParsingError(tag: String, exception: Exception, rawJson: String) {
        Log.e(tag, "JSON Parsing Error Details:")
        Log.e(tag, "Exception type: ${exception.javaClass.simpleName}")
        Log.e(tag, "Exception message: ${exception.message}")
        Log.e(tag, "JSON length: ${rawJson.length}")
        Log.e(tag, "JSON first 100 chars: ${rawJson.take(100)}")
        Log.e(tag, "JSON last 100 chars: ${rawJson.takeLast(100)}")
        Log.e(tag, "Starts with '{': ${rawJson.trim().startsWith("{")}")
        Log.e(tag, "Ends with '}': ${rawJson.trim().endsWith("}")}")
        Log.e(tag, "Contains 'error': ${rawJson.contains("error", ignoreCase = true)}")
    }
}
package com.grupo03.solea.utils

import android.util.Patterns
import com.grupo03.solea.data.models.ValidationConstants

object Validation {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= ValidationConstants.MIN_PASSWORD_LENGTH &&
                password.length <= ValidationConstants.MAX_PASSWORD_LENGTH
    }

    fun isValidName(name: String): Boolean {
        return name.matches(Regex(ValidationConstants.NAME_REGEX))
    }
}
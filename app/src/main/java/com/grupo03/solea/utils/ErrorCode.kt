package com.grupo03.solea.utils

import androidx.annotation.StringRes
import com.grupo03.solea.R

object ErrorCode {
    enum class Auth {
        USER_NOT_FOUND,
        INVALID_CREDENTIALS,
        USER_COLLISION,
        EMAIL_ERROR,
        UNKNOWN_ERROR,
        INVALID_EMAIL,
        WEAK_PASSWORD,
        GOOGLE_SIGN_IN_FAILED
    }
}

@StringRes
fun ErrorCode.Auth.getStringRes(): Int {
    return when (this) {
        ErrorCode.Auth.USER_NOT_FOUND -> R.string.error_auth_user_not_found
        ErrorCode.Auth.INVALID_CREDENTIALS -> R.string.error_auth_invalid_credentials
        ErrorCode.Auth.USER_COLLISION -> R.string.error_auth_user_collision
        ErrorCode.Auth.EMAIL_ERROR -> R.string.error_auth_email
        ErrorCode.Auth.UNKNOWN_ERROR -> R.string.error_auth_unknown
        ErrorCode.Auth.INVALID_EMAIL -> R.string.error_auth_invalid_email
        ErrorCode.Auth.WEAK_PASSWORD -> R.string.error_auth_weak_password
        ErrorCode.Auth.GOOGLE_SIGN_IN_FAILED -> R.string.error_auth_google_sign_in_failed
    }
}
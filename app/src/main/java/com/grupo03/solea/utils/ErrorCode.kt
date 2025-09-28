package com.grupo03.solea.utils

import androidx.annotation.StringRes
import com.grupo03.solea.R

object ErrorCode {
    enum class Auth {
        USER_NOT_FOUND,
        USER_COLLISION,
        USERNAME_INVALID,
        INVALID_CREDENTIALS,
        EMAIL_ERROR,
        EMAIL_EMPTY,
        EMAIL_INVALID,
        EMAIL_TOO_LONG,
        UNKNOWN_ERROR,
        WEAK_PASSWORD,
        PASSWORD_EMPTY,
        GOOGLE_SIGN_IN_FAILED,
        PASSWORDS_DO_NOT_MATCH
    }
}

@StringRes
fun ErrorCode.Auth.getStringRes(): Int {
    return when (this) {
        ErrorCode.Auth.USER_NOT_FOUND -> R.string.error_auth_user_not_found
        ErrorCode.Auth.INVALID_CREDENTIALS -> R.string.error_auth_invalid_credentials
        ErrorCode.Auth.USER_COLLISION -> R.string.error_auth_user_collision
        ErrorCode.Auth.USERNAME_INVALID -> R.string.error_auth_invalid_username
        ErrorCode.Auth.EMAIL_ERROR -> R.string.error_auth_email
        ErrorCode.Auth.EMAIL_INVALID -> R.string.error_auth_invalid_email
        ErrorCode.Auth.EMAIL_EMPTY -> R.string.error_auth_email_empty
        ErrorCode.Auth.EMAIL_TOO_LONG -> R.string.error_auth_email_too_long
        ErrorCode.Auth.UNKNOWN_ERROR -> R.string.error_auth_unknown
        ErrorCode.Auth.WEAK_PASSWORD -> R.string.error_auth_weak_password
        ErrorCode.Auth.PASSWORD_EMPTY -> R.string.error_auth_password_empty
        ErrorCode.Auth.GOOGLE_SIGN_IN_FAILED -> R.string.error_auth_google_sign_in_failed
        ErrorCode.Auth.PASSWORDS_DO_NOT_MATCH -> R.string.error_auth_passwords_do_not_match
    }
}

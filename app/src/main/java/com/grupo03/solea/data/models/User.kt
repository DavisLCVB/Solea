package com.grupo03.solea.data.models

import com.grupo03.solea.utils.ErrorCode

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null
)

data class AuthResult(
    val success: Boolean,
    val user: User? = null,
    val errorCode: ErrorCode.Auth? = null
)
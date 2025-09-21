package com.grupo03.solea.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null
)

data class AuthResult(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null
)

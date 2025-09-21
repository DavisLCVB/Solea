package com.grupo03.solea.data.models

object ValidationConstants {
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 20
    const val NAME_REGEX = "^[a-zA-ZÀ-ÿ\\s]{1,40}$"
}

object DatabaseConstants {
    const val USERS_COLLECTION = "users"
}
package com.grupo03.solea.utils

object ValidationConstants {
    const val MAX_EMAIL_LENGTH = 254
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 20
    const val NAME_REGEX = "^[a-zA-ZÀ-ÿ0-9\\s.]{3,40}$"
}

object ServiceConstants {
    const val WEB_CLIENT_ID =
        "605623344017-j1n9ujjcf29f4q3mfa41lf79jv7q7b4s.apps.googleusercontent.com"
}

object DatabaseContants {
    const val USERS_COLLECTION = "users"
    const val MOVEMENTS_COLLECTION = "movements"
    const val BUDGETS_COLLECTION = "budgets"
    const val STATUS_COLLECTION = "status"
    const val CATEGORIES_COLLECTION = "categories"
}
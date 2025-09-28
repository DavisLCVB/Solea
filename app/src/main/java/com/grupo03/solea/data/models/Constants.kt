package com.grupo03.solea.data.models

object ValidationConstants {
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 20
    const val NAME_REGEX = "^[a-zA-ZÀ-ÿ\\s]{1,40}$"
}

object ServiceConstants {
    const val WEB_CLIENT_ID =
        "605623344017-j1n9ujjcf29f4q3mfa41lf79jv7q7b4s.apps.googleusercontent.com"
}

object DatabaseContants {
    const val MOVEMENTS_COLLECTION = "movements"
    const val MOVEMENT_TYPES_COLLECTION = "movement_types"
}
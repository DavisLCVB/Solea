package com.grupo03.solea.utils

/**
 * Constants for input validation.
 */
object ValidationConstants {
    /** Maximum allowed length for email addresses */
    const val MAX_EMAIL_LENGTH = 254

    /** Minimum allowed length for passwords */
    const val MIN_PASSWORD_LENGTH = 6

    /** Maximum allowed length for passwords */
    const val MAX_PASSWORD_LENGTH = 20

    /**
     * Regular expression for validating names.
     * Allows letters (including accented), numbers, spaces, and periods.
     * Length must be between 3 and 40 characters.
     */
    const val NAME_REGEX = "^[a-zA-ZÀ-ÿ0-9\\s.]{3,40}$"
}

/**
 * Constants for external services configuration.
 */
object ServiceConstants {
    /**
     * Google OAuth Web Client ID for authentication.
     * This is used for Google Sign-In functionality.
     */
    const val WEB_CLIENT_ID =
        "605623344017-j1n9ujjcf29f4q3mfa41lf79jv7q7b4s.apps.googleusercontent.com"
}

/**
 * Constants for Firestore database collection names.
 */
object DatabaseContants {

    /** Collection name for budget documents */
    const val BUDGETS_COLLECTION = "budgets"

    /** Collection name for budget status documents */
    const val STATUS_COLLECTION = "status"

    /** Collection name for category documents */
    const val CATEGORIES_COLLECTION = "categories"
}

/**
 * Constants for AI/ML services.
 */
object AIConstants {
    /**
     * Base URL for the receipt scanner API.
     * This API uses AI/OCR to extract data from receipt images.
     */
    const val RECEIPT_SCANNER_API_URL = "https://gemini-py.onrender.com/"
}

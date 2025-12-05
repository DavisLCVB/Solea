package com.grupo03.solea.data.models

/**
 * Represents a user in the application.
 *
 * This data class holds the basic user information obtained from Firebase Authentication
 * and additional profile data stored in Firestore.
 * Users are authenticated via email/password or Google OAuth.
 *
 * @property uid Unique user identifier from Firebase Authentication
 * @property email User's email address
 * @property displayName User's display name (optional, may be null for email/password users)
 * @property photoUrl URL to user's profile photo (optional, typically from Google accounts)
 * @property currency User's preferred currency code (ISO 4217, e.g., "USD", "PEN", "EUR").
 *                   Set once during registration and cannot be changed afterward.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val currency: String = "USD"
)

package com.grupo03.solea.data.models

/**
 * Represents a category for classifying financial movements.
 *
 * Categories can be either global (default, available to all users) or
 * user-specific (custom categories created by individual users).
 *
 * @property id Unique identifier for the category
 * @property name Name of the category (e.g., "Food", "Transportation")
 * @property description Description that AI uses to suggest categories
 * @property userId ID of the user who created this category. Null for default/global categories
 */
data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val userId: String? = null
)

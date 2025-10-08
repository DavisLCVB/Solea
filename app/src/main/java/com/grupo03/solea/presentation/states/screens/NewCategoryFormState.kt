package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.utils.AppError

/**
 * UI state for the new category form.
 *
 * Manages the state of creating a new custom category, including
 * form fields, validation, and operation status.
 *
 * @property name Category name
 * @property description Category description (used by AI for categorization suggestions)
 * @property isNameValid Whether the category name is valid
 * @property isLoading Whether a save operation is in progress
 * @property error Error that occurred during save, null if no error
 * @property successMessage Success message to display, null if none
 */
data class NewCategoryFormState(
    val name: String = "",
    val description: String = "",
    val isNameValid: Boolean = true,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val successMessage: String? = null
)

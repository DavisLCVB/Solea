package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.repositories.interfaces.CategoryRepository
import com.grupo03.solea.presentation.states.screens.NewCategoryFormState
import com.grupo03.solea.utils.CategoryError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the new category creation form.
 *
 * Manages the state of creating new custom categories, including form validation
 * and submission. Also supports batch creation of AI-suggested categories.
 *
 * @property categoryRepository Repository for category operations
 */
class NewCategoryFormViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    /** Form state including field values, validation, and operation status */
    private val _formState = MutableStateFlow(NewCategoryFormState())
    val formState: StateFlow<NewCategoryFormState> = _formState.asStateFlow()

    /**
     * Handles category name field changes with validation.
     *
     * Category names must be at least 3 characters long and non-blank.
     *
     * @param newName The new category name value
     */
    fun onNameChange(newName: String) {
        val isValid = newName.isNotBlank() && newName.length >= 3
        _formState.value = _formState.value.copy(
            name = newName,
            isNameValid = isValid,
            error = if (!isValid && newName.isNotEmpty()) CategoryError.INVALID_NAME else null
        )
    }

    /**
     * Handles category description field changes.
     *
     * The description is used by AI for suggesting categories automatically.
     *
     * @param newDescription The new description value
     */
    fun onDescriptionChange(newDescription: String) {
        _formState.value = _formState.value.copy(
            description = newDescription
        )
    }

    /**
     * Creates a new category.
     *
     * Validates the category name before attempting to create. The category is checked
     * for duplicates in the repository (see FirebaseCategoryRepository.createCategory).
     *
     * @param userId The ID of the user creating the category
     * @param onSuccess Callback invoked when category is created successfully
     */
    fun createCategory(userId: String, onSuccess: () -> Unit) {
        val currentState = _formState.value

        // Validate
        if (currentState.name.isBlank() || currentState.name.length < 3) {
            _formState.value = currentState.copy(
                isNameValid = false,
                error = CategoryError.INVALID_NAME
            )
            return
        }

        _formState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val category = Category(
                id = UUID.randomUUID().toString(),
                name = currentState.name,
                description = currentState.description,
                userId = userId
            )

            val result = categoryRepository.createCategory(category)

            if (result.isSuccess) {
                _formState.value = NewCategoryFormState(
                    successMessage = "Categoría creada exitosamente"
                )
                onSuccess()
            } else {
                _formState.value = currentState.copy(
                    isLoading = false,
                    error = result.errorOrNull()
                )
            }
        }
    }

    /**
     * Clears the form state, resetting all fields to default values.
     */
    fun clearForm() {
        _formState.value = NewCategoryFormState()
    }

    /**
     * Creates multiple categories in batch (used for AI-suggested categories).
     *
     * Iterates through the provided category names and creates each one. Stops
     * on first error or continues until all are created. Category names shorter
     * than 3 characters are skipped.
     *
     * @param userId The user ID to associate the categories with
     * @param categoryNames List of category names to create
     * @param onSuccess Callback when all categories are created successfully
     * @param onError Callback when there's an error, receives error message
     */
    fun createCategoriesInBatch(
        userId: String,
        categoryNames: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (categoryNames.isEmpty()) {
            onSuccess()
            return
        }

        viewModelScope.launch {
            val description = "Category automatically detected by AI"
            var successCount = 0
            var errorOccurred = false

            for (categoryName in categoryNames) {
                // Skip if category name is too short
                if (categoryName.length < 3) continue

                val category = Category(
                    id = UUID.randomUUID().toString(),
                    name = categoryName,
                    description = description,
                    userId = userId
                )

                val result = categoryRepository.createCategory(category)

                if (result.isSuccess) {
                    successCount++
                } else {
                    errorOccurred = true
                    onError("Failed to create category: $categoryName")
                    break
                }
            }

            if (!errorOccurred && successCount > 0) {
                onSuccess()
            } else if (!errorOccurred && successCount == 0) {
                onError("No valid categories to create")
            }
        }
    }
}

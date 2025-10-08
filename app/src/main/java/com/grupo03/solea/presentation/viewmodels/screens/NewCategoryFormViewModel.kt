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

class NewCategoryFormViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(NewCategoryFormState())
    val formState: StateFlow<NewCategoryFormState> = _formState.asStateFlow()

    fun onNameChange(newName: String) {
        val isValid = newName.isNotBlank() && newName.length >= 3
        _formState.value = _formState.value.copy(
            name = newName,
            isNameValid = isValid,
            error = if (!isValid && newName.isNotEmpty()) CategoryError.INVALID_NAME else null
        )
    }

    fun onDescriptionChange(newDescription: String) {
        _formState.value = _formState.value.copy(
            description = newDescription
        )
    }

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

    fun clearForm() {
        _formState.value = NewCategoryFormState()
    }

    /**
     * Creates multiple categories in batch (used for AI-suggested categories)
     * @param userId The user ID to associate the categories with
     * @param categoryNames List of category names to create
     * @param onSuccess Callback when all categories are created successfully
     * @param onError Callback when there's an error
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

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
}

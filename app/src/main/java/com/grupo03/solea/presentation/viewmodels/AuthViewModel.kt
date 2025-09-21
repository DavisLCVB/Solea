package com.grupo03.solea.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.repositories.AuthRepository
import com.grupo03.solea.presentation.states.AuthUiState
import com.grupo03.solea.utils.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.value = _uiState.value.copy(isLoggedIn = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoggedIn = false)
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        val errorMessage = if (Validation.isValidEmail(newEmail)) null else "invalid_email"
        _uiState.value =
            _uiState.value.copy(
                email = newEmail,
                isEmailValid = Validation.isValidEmail(newEmail),
                errorMessage = errorMessage
            )
    }

    fun onPasswordChange(newPassword: String) {
        val errorMessage =
            if (Validation.isValidPassword(newPassword)) null else "invalid_password"
        _uiState.value = _uiState.value.copy(
            password = newPassword,
            isPasswordValid = Validation.isValidPassword(newPassword),
            errorMessage = errorMessage
        )
    }

    fun signIn() {
        val currentState = _uiState.value

        if (!Validation.isValidEmail(currentState.email)) {
            _uiState.value = currentState.copy(isEmailValid = false, errorMessage = "invalid_email")
            return
        }

        if (!Validation.isValidPassword(currentState.password)) {
            _uiState.value =
                currentState.copy(isPasswordValid = false, errorMessage = "invalid_password")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            val result = authRepository.signIn(_uiState.value.email, _uiState.value.password)
            if (result.success) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
            } else {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isLoggedIn = false
                    )
            }
        }
    }

    fun signUp() {
        val currentState = _uiState.value

        if (!Validation.isValidEmail(currentState.email)) {
            _uiState.value = currentState.copy(isEmailValid = false, errorMessage = "invalid_email")
            return
        }

        if (!Validation.isValidPassword(currentState.password)) {
            _uiState.value =
                currentState.copy(isPasswordValid = false, errorMessage = "invalid_password")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            val result = authRepository.signUp(_uiState.value.email, _uiState.value.password)
            if (result.success) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
            } else {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isLoggedIn = false
                    )
            }
        }
    }

    fun signOut() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.signOut()
            if (result.success) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = false)
            } else {
                _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}
package com.grupo03.solea.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.repositories.AuthRepository
import com.grupo03.solea.presentation.states.AuthUiState
import com.grupo03.solea.utils.ErrorCode
import com.grupo03.solea.utils.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        val errorMessage =
            if (Validation.isValidEmail(newEmail)) null else ErrorCode.Auth.INVALID_EMAIL
        _uiState.value =
            _uiState.value.copy(
                email = newEmail,
                isEmailValid = Validation.isValidEmail(newEmail),
                errorCode = errorMessage
            )
    }

    fun onPasswordChange(newPassword: String, skipValidation: Boolean = false) {
        val errorMessage =
            if (Validation.isValidPassword(newPassword)) null else ErrorCode.Auth.WEAK_PASSWORD
        _uiState.value = _uiState.value.copy(
            password = newPassword,
            isPasswordValid = Validation.isValidPassword(newPassword) || skipValidation,
            errorCode = if (skipValidation) null else errorMessage
        )
    }

    fun signInWithEmailAndPassword() {
        val currentState = _uiState.value

        if (!Validation.isValidEmail(currentState.email)) {
            _uiState.update {
                it.copy(
                    isEmailValid = false,
                    errorCode = ErrorCode.Auth.INVALID_EMAIL
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorCode = null) }

            val result =
                authRepository.signInWithEmailAndPassword(currentState.email, currentState.password)

            _uiState.update {
                if (result.success) {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorCode = null
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorCode = result.errorCode,
                        isLoggedIn = false
                    )
                }
            }
        }
    }

    fun signUpWithEmailAndPassword() {
        val currentState = _uiState.value

        if (!Validation.isValidEmail(currentState.email)) {
            _uiState.update {
                it.copy(
                    isEmailValid = false,
                    errorCode = ErrorCode.Auth.INVALID_EMAIL
                )
            }
            return
        }

        if (!Validation.isValidPassword(currentState.password)) {
            _uiState.update {
                it.copy(
                    isPasswordValid = false,
                    errorCode = ErrorCode.Auth.WEAK_PASSWORD
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorCode = null) }

            val result =
                authRepository.signUpWithEmailAndPassword(currentState.email, currentState.password)

            _uiState.update {
                if (result.success) {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorCode = null
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorCode = result.errorCode,
                        isLoggedIn = false
                    )
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorCode = null) }

            val result = authRepository.signInWithGoogle(idToken)

            _uiState.update {
                if (result.success) {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorCode = null
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorCode = result.errorCode,
                        isLoggedIn = false
                    )
                }
            }
        }
    }

    fun signOut() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorCode = null)
        viewModelScope.launch {
            val result = authRepository.signOut()
            if (result.success) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = false)
            } else {
                _uiState.value =
                    _uiState.value.copy(isLoading = false, errorCode = result.errorCode)
            }
        }
    }


}
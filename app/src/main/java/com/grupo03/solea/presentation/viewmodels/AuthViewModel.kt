package com.grupo03.solea.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.User
import com.grupo03.solea.data.services.AuthService
import com.grupo03.solea.presentation.states.AuthState
import com.grupo03.solea.utils.ErrorCode
import com.grupo03.solea.utils.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState.State())
    val uiState: StateFlow<AuthState.State> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun setUser(user: User?) {
        _uiState.update { it.copy(user = user) }
    }

    private fun setSignInForm(change: (AuthState.SignInFormState) -> AuthState.SignInFormState) {
        val newLoginFormState = change(_uiState.value.signInFormState)
        _uiState.update { it.copy(signInFormState = newLoginFormState) }
    }

    private fun setSignUpForm(change: (AuthState.SignUpFormState) -> AuthState.SignUpFormState) {
        val newSignUpForm = change(_uiState.value.signUpFormState)
        _uiState.update { it.copy(signUpFormState = newSignUpForm) }
    }

    private fun setErrorCode(errorCode: ErrorCode.Auth?) {
        _uiState.update { it.copy(errorCode = errorCode) }
    }

    private fun isEmailError(): Boolean {
        return _uiState.value.errorCode == ErrorCode.Auth.EMAIL_INVALID ||
                _uiState.value.errorCode == ErrorCode.Auth.EMAIL_ERROR ||
                _uiState.value.errorCode == ErrorCode.Auth.EMAIL_EMPTY ||
                _uiState.value.errorCode == ErrorCode.Auth.EMAIL_TOO_LONG
    }

    private fun isPasswordError(): Boolean {
        return _uiState.value.errorCode == ErrorCode.Auth.WEAK_PASSWORD ||
                _uiState.value.errorCode == ErrorCode.Auth.PASSWORDS_DO_NOT_MATCH ||
                _uiState.value.errorCode == ErrorCode.Auth.PASSWORD_EMPTY
    }

    private fun isUsernameError(): Boolean {
        return _uiState.value.errorCode == ErrorCode.Auth.USERNAME_INVALID
    }

    fun checkAuthState() {
        setLoading(true)
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            setUser(user)
            setLoading(false)
        }
    }

    fun onEmailChange(formType: AuthState.FormType, newEmail: String) {
        when (formType) {
            AuthState.FormType.LOGIN -> onSignInEmailChange(newEmail)
            AuthState.FormType.REGISTER -> onSignUpEmailChange(newEmail)
        }
    }

    private fun onSignInEmailChange(newEmail: String) {
        val errorCode = Validation.checkEmail(newEmail)
        setSignInForm {
            it.copy(
                email = newEmail,
                isEmailValid = errorCode == null,
            )
        }
        if (errorCode != null || isEmailError()) {
            setErrorCode(errorCode)
            if (errorCode == null) {
                setSignInForm {
                    it.copy(isEmailValid = true)
                }
            }
        }
    }

    private fun onSignUpEmailChange(newEmail: String) {
        val errorCode = Validation.checkEmail(newEmail)
        setSignUpForm {
            it.copy(
                email = newEmail,
                isEmailValid = errorCode == null,
            )
        }
        if (errorCode != null || isEmailError()) {
            setErrorCode(errorCode)
            if (errorCode == null) {
                setSignUpForm {
                    it.copy(isEmailValid = true)
                }
            }
        }
    }

    fun onSignUpNameChange(newName: String) {
        val errorCode = Validation.checkName(newName)
        setSignUpForm {
            it.copy(
                name = newName,
                isNameValid = errorCode == null,
            )
        }
        if (errorCode != null || isUsernameError()) {
            setErrorCode(errorCode)
            if (errorCode == null) {
                setSignUpForm {
                    it.copy(isNameValid = true)
                }
            }
        }
    }

    fun onPasswordChange(formType: AuthState.FormType, newPassword: String) {
        when (formType) {
            AuthState.FormType.LOGIN -> onSignInPasswordChange(newPassword)
            AuthState.FormType.REGISTER -> onSignUpPasswordChange(newPassword)
        }
    }

    private fun onSignInPasswordChange(newPassword: String) {
        setSignInForm {
            it.copy(
                password = newPassword,
            )
        }
    }

    private fun onSignUpPasswordChange(newPassword: String) {
        val errorCode = Validation.checkPassword(newPassword)
        setSignUpForm {
            it.copy(
                password = newPassword,
                isPasswordValid = errorCode == null,
            )
        }
        if (errorCode != null || isPasswordError()) {
            setErrorCode(errorCode)
            if (errorCode == null) {
                setSignUpForm {
                    it.copy(isPasswordValid = true)
                }
            }
        }
    }

    fun onSignUpConfirmPasswordChange(newPassword: String) {
        setSignUpForm {
            it.copy(
                confirmPassword = newPassword,
            )
        }
        if (newPassword != _uiState.value.signUpFormState.password) {
            setErrorCode(ErrorCode.Auth.PASSWORDS_DO_NOT_MATCH)
            setSignUpForm {
                it.copy(isPasswordValid = false)
            }
        } else if (_uiState.value.errorCode == ErrorCode.Auth.PASSWORDS_DO_NOT_MATCH) {
            setErrorCode(null)
            setSignUpForm {
                it.copy(isPasswordValid = true)
            }
        }
    }

    fun signInWithEmailAndPassword() {
        val formState = _uiState.value.signInFormState
        if (!formState.isEmailValid) {
            return
        }
        viewModelScope.launch {
            setErrorCode(null)
            setLoading(true)
            val result =
                authRepository.signInWithEmailAndPassword(
                    formState.email,
                    formState.password
                )
            setUser(result.user)
            setErrorCode(result.errorCode)
            setLoading(false)
        }
    }

    fun signUpWithEmailAndPassword() {
        val formState = _uiState.value.signUpFormState
        if (!formState.isEmailValid || !formState.isPasswordValid) {
            return
        }
        viewModelScope.launch {
            setLoading(true)
            setErrorCode(null)
            val result =
                authRepository.signUpWithEmailAndPassword(
                    formState.email,
                    formState.password
                )
            setErrorCode(result.errorCode)
            setUser(result.user)
            setLoading(false)
        }
    }

    fun signInWithGoogle(context: Context) {
        val request = authRepository.generateGoogleRequest()
        if (request == null) {
            setErrorCode(ErrorCode.Auth.GOOGLE_SIGN_IN_FAILED)
            return
        }
        viewModelScope.launch {
            setLoading(true)
            setErrorCode(null)
            val result = authRepository.signInWithGoogle(context, request)
            setUser(result.user)
            setErrorCode(result.errorCode)
            setLoading(false)
        }
    }

    fun signOut() {
        setLoading(true)
        setErrorCode(null)
        viewModelScope.launch {
            val result = authRepository.signOut()
            if (result.errorCode == null) {
                setUser(null)
            } else {
                setErrorCode(result.errorCode)
            }
            setLoading(false)
        }
    }


}
package com.grupo03.solea.presentation.viewmodels.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.User
import com.grupo03.solea.data.services.interfaces.AuthService
import com.grupo03.solea.presentation.states.screens.SignInFormState
import com.grupo03.solea.presentation.states.screens.SignUpFormState
import com.grupo03.solea.presentation.states.shared.AuthState
import com.grupo03.solea.presentation.states.shared.FormType
import com.grupo03.solea.utils.AuthError
import com.grupo03.solea.utils.AuthResult
import com.grupo03.solea.utils.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private val _signInFormState = MutableStateFlow(SignInFormState())
    val signInFormState: StateFlow<SignInFormState> = _signInFormState.asStateFlow()
    private val _signUpFormState = MutableStateFlow(SignUpFormState())
    val signUpFormState: StateFlow<SignUpFormState> = _signUpFormState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun setLoading(isLoading: Boolean, formType: FormType?) {
        if (formType == null) {
            _signInFormState.update { it.copy(isLoading = isLoading) }
            _signUpFormState.update { it.copy(isLoading = isLoading) }
            return
        }
        when (formType) {
            FormType.SIGN_IN -> {
                _signInFormState.update { it.copy(isLoading = isLoading) }
            }

            FormType.SIGN_UP -> {
                _signUpFormState.update { it.copy(isLoading = isLoading) }
            }
        }
    }

    private fun setUser(user: User?) {
        _authState.update { it.copy(user = user) }
    }

    private fun setSignInForm(change: (SignInFormState) -> SignInFormState) {
        _signInFormState.value = change(_signInFormState.value)
    }

    private fun setSignUpForm(change: (SignUpFormState) -> SignUpFormState) {
        _signUpFormState.value = change(_signUpFormState.value)
    }

    private fun setErrorCode(errorCode: AuthError?) {
        _authState.update { it.copy(errorCode = errorCode) }
    }

    private fun isEmailError(): Boolean {
        return _authState.value.errorCode == AuthError.EMAIL_INVALID ||
                _authState.value.errorCode == AuthError.EMAIL_ERROR ||
                _authState.value.errorCode == AuthError.EMAIL_EMPTY ||
                _authState.value.errorCode == AuthError.EMAIL_TOO_LONG
    }

    private fun isPasswordError(): Boolean {
        return _authState.value.errorCode == AuthError.WEAK_PASSWORD ||
                _authState.value.errorCode == AuthError.PASSWORDS_DO_NOT_MATCH ||
                _authState.value.errorCode == AuthError.PASSWORD_EMPTY
    }

    private fun isUsernameError(): Boolean {
        return _authState.value.errorCode == AuthError.USERNAME_INVALID
    }

    fun checkAuthState() {
        setLoading(true, null)
        viewModelScope.launch {
            val user = authService.getCurrentUser()
            setUser(user)
            setLoading(false, null)
        }
    }

    fun onEmailChange(formType: FormType, newEmail: String) {
        when (formType) {
            FormType.SIGN_IN -> onSignInEmailChange(newEmail)
            FormType.SIGN_UP -> onSignUpEmailChange(newEmail)
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

    fun onSignUpPhotoChange(photoUri: String?) {
        setSignUpForm {
            it.copy(photoUri = photoUri)
        }
    }

    fun onPasswordChange(formType: FormType, newPassword: String) {
        when (formType) {
            FormType.SIGN_IN -> onSignInPasswordChange(newPassword)
            FormType.SIGN_UP -> onSignUpPasswordChange(newPassword)
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
        if (newPassword != _signUpFormState.value.password) {
            setErrorCode(AuthError.PASSWORDS_DO_NOT_MATCH)
            setSignUpForm {
                it.copy(isPasswordValid = false)
            }
        } else if (_authState.value.errorCode == AuthError.PASSWORDS_DO_NOT_MATCH) {
            setErrorCode(null)
            setSignUpForm {
                it.copy(isPasswordValid = true)
            }
        }
    }

    fun signInWithEmailAndPassword() {
        val formState = _signInFormState.value
        if (!formState.isEmailValid) {
            return
        }
        viewModelScope.launch {
            setErrorCode(null)
            setLoading(true, FormType.SIGN_IN)
            val result =
                authService.signInWithEmailAndPassword(
                    formState.email,
                    formState.password
                )
            result.onSuccess { user, _ ->
                setUser(user)
            }.onError { error ->
                setErrorCode(error)
            }
            setLoading(false, FormType.SIGN_IN)
        }
    }

    fun signUpWithEmailAndPassword() {
        val formState = _signUpFormState.value
        if (!formState.isEmailValid || !formState.isPasswordValid || !formState.isNameValid) {
            return
        }
        viewModelScope.launch {
            setLoading(true, FormType.SIGN_UP)
            setErrorCode(null)
            val result =
                authService.signUpWithEmailAndPassword(
                    email = formState.email,
                    password = formState.password,
                    displayName = formState.name
                )
            result.onSuccess { user, _ ->
                setUser(user)
            }.onError { error ->
                setErrorCode(error)
            }
            setLoading(false, FormType.SIGN_UP)
        }
    }

    fun signInWithGoogle(context: Context) {
        val request = authService.generateGoogleRequest()
        if (request == null) {
            setErrorCode(AuthError.GOOGLE_SIGN_IN_FAILED)
            return
        }
        viewModelScope.launch {
            setLoading(true, FormType.SIGN_IN)
            setErrorCode(null)
            val result = authService.signInWithGoogle(context, request)
            result.onSuccess { user, _ ->
                setUser(user)
            }.onError { error ->
                setErrorCode(error)
            }
            setLoading(false, FormType.SIGN_IN)
        }
    }

    fun signOut() {
        setLoading(true, null)
        setErrorCode(null)
        viewModelScope.launch {
            val result = authService.signOut()
            result.onSuccess { _, _ ->
                setUser(null)
            }.onError { error ->
                setErrorCode(error)
            }
            setLoading(false, null)
        }
    }


}
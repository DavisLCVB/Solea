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

/**
 * ViewModel for managing authentication state and operations.
 *
 * Handles user authentication including email/password sign-in, email/password registration,
 * Google OAuth sign-in, and sign-out. Manages three separate UI states: authentication state,
 * sign-in form state, and sign-up form state.
 *
 * This ViewModel provides real-time validation for form fields and coordinates between
 * the AuthService and the UI layer, exposing StateFlows for reactive UI updates.
 *
 * @property authService Service for performing authentication operations
 */
class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    /** Authentication state including current user and error status */
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /** Sign-in form state including field values, validation, and loading status */
    private val _signInFormState = MutableStateFlow(SignInFormState())
    val signInFormState: StateFlow<SignInFormState> = _signInFormState.asStateFlow()

    /** Sign-up form state including field values, validation, and loading status */
    private val _signUpFormState = MutableStateFlow(SignUpFormState())
    val signUpFormState: StateFlow<SignUpFormState> = _signUpFormState.asStateFlow()

    init {
        checkAuthState()
    }

    /**
     * Sets loading state for authentication forms.
     *
     * @param isLoading Whether a loading operation is in progress
     * @param formType Which form to update (SIGN_IN, SIGN_UP), or null to update both
     */
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

    /**
     * Updates the current authenticated user in the auth state.
     *
     * @param user The authenticated user, or null if signed out
     */
    private fun setUser(user: User?) {
        _authState.update { it.copy(user = user) }
    }

    /**
     * Updates the sign-in form state.
     *
     * @param change Function to transform the current sign-in form state
     */
    private fun setSignInForm(change: (SignInFormState) -> SignInFormState) {
        _signInFormState.value = change(_signInFormState.value)
    }

    /**
     * Updates the sign-up form state.
     *
     * @param change Function to transform the current sign-up form state
     */
    private fun setSignUpForm(change: (SignUpFormState) -> SignUpFormState) {
        _signUpFormState.value = change(_signUpFormState.value)
    }

    /**
     * Sets the current authentication error.
     *
     * @param errorCode The authentication error, or null to clear the error
     */
    private fun setErrorCode(errorCode: AuthError?) {
        _authState.update { it.copy(errorCode = errorCode) }
    }

    /**
     * Checks if the current error is related to email validation.
     *
     * @return True if the current error is an email-related error
     */
    private fun isEmailError(): Boolean {
        return _authState.value.errorCode == AuthError.EMAIL_INVALID ||
                _authState.value.errorCode == AuthError.EMAIL_ERROR ||
                _authState.value.errorCode == AuthError.EMAIL_EMPTY ||
                _authState.value.errorCode == AuthError.EMAIL_TOO_LONG
    }

    /**
     * Checks if the current error is related to password validation.
     *
     * @return True if the current error is a password-related error
     */
    private fun isPasswordError(): Boolean {
        return _authState.value.errorCode == AuthError.WEAK_PASSWORD ||
                _authState.value.errorCode == AuthError.PASSWORDS_DO_NOT_MATCH ||
                _authState.value.errorCode == AuthError.PASSWORD_EMPTY
    }

    /**
     * Checks if the current error is related to username validation.
     *
     * @return True if the current error is a username-related error
     */
    private fun isUsernameError(): Boolean {
        return _authState.value.errorCode == AuthError.USERNAME_INVALID
    }

    /**
     * Checks and updates the current authentication state.
     *
     * Queries the auth service for the current user and updates the auth state accordingly.
     * Called automatically during initialization and can be called manually to refresh state.
     */
    fun checkAuthState() {
        setLoading(true, null)
        viewModelScope.launch {
            val user = authService.getCurrentUser()
            setUser(user)
            setLoading(false, null)
        }
    }

    /**
     * Handles email field changes for authentication forms.
     *
     * Delegates to the appropriate handler based on form type and performs real-time validation.
     *
     * @param formType The form being updated (SIGN_IN or SIGN_UP)
     * @param newEmail The new email value
     */
    fun onEmailChange(formType: FormType, newEmail: String) {
        when (formType) {
            FormType.SIGN_IN -> onSignInEmailChange(newEmail)
            FormType.SIGN_UP -> onSignUpEmailChange(newEmail)
        }
    }

    /**
     * Handles email changes in the sign-in form with validation.
     *
     * @param newEmail The new email value
     */
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

    /**
     * Handles email changes in the sign-up form with validation.
     *
     * @param newEmail The new email value
     */
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

    /**
     * Handles name field changes in the sign-up form with validation.
     *
     * @param newName The new name value
     */
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

    /**
     * Handles photo URI changes in the sign-up form.
     *
     * @param photoUri The new photo URI value, or null to remove
     */
    fun onSignUpPhotoChange(photoUri: String?) {
        setSignUpForm {
            it.copy(photoUri = photoUri)
        }
    }

    /**
     * Handles password field changes for authentication forms.
     *
     * Delegates to the appropriate handler based on form type. Sign-up includes validation.
     *
     * @param formType The form being updated (SIGN_IN or SIGN_UP)
     * @param newPassword The new password value
     */
    fun onPasswordChange(formType: FormType, newPassword: String) {
        when (formType) {
            FormType.SIGN_IN -> onSignInPasswordChange(newPassword)
            FormType.SIGN_UP -> onSignUpPasswordChange(newPassword)
        }
    }

    /**
     * Handles password changes in the sign-in form.
     *
     * @param newPassword The new password value
     */
    private fun onSignInPasswordChange(newPassword: String) {
        setSignInForm {
            it.copy(
                password = newPassword,
            )
        }
    }

    /**
     * Handles password changes in the sign-up form with validation.
     *
     * @param newPassword The new password value
     */
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

    /**
     * Handles confirm password field changes in the sign-up form.
     *
     * Validates that the confirmation password matches the main password.
     *
     * @param newPassword The new confirm password value
     */
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

    /**
     * Performs sign-in with email and password.
     *
     * Validates that the email is valid, then attempts to sign in using the auth service.
     * Updates the auth state with the user on success or error on failure.
     */
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

    /**
     * Performs sign-up with email and password.
     *
     * Validates all form fields (email, name, password, password confirmation) before
     * attempting to create a new account using the auth service. Updates the auth state
     * with the new user on success or error on failure.
     */
    fun signUpWithEmailAndPassword() {
        val formState = _signUpFormState.value

        // Validar email
        if (!formState.isEmailValid) {
            val emailError = Validation.checkEmail(formState.email)
            setErrorCode(emailError ?: AuthError.EMAIL_INVALID)
            return
        }

        // Validar nombre
        if (!formState.isNameValid) {
            val nameError = Validation.checkName(formState.name)
            setErrorCode(nameError ?: AuthError.USERNAME_INVALID)
            return
        }

        // Validar contraseña
        if (!formState.isPasswordValid) {
            val passwordError = Validation.checkPassword(formState.password)
            setErrorCode(passwordError ?: AuthError.WEAK_PASSWORD)
            return
        }

        // Validar que las contraseñas coincidan
        if (formState.password != formState.confirmPassword) {
            setErrorCode(AuthError.PASSWORDS_DO_NOT_MATCH)
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

    /**
     * Performs sign-in with Google OAuth.
     *
     * Generates a Google sign-in request and attempts to authenticate using the Google
     * identity provider. Requires an Android context for the Google sign-in flow.
     *
     * @param context Android context required for Google sign-in
     */
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

    /**
     * Signs out the current user.
     *
     * Clears the user from the auth state and signs out from all providers
     * (email/password and Google).
     */
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
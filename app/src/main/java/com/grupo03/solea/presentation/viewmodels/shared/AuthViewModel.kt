package com.grupo03.solea.presentation.viewmodels.shared

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.User
import com.grupo03.solea.data.repositories.interfaces.UserRepository
import com.grupo03.solea.data.services.interfaces.AuthService
import com.grupo03.solea.presentation.states.screens.SignInFormState
import com.grupo03.solea.presentation.states.screens.SignUpFormState
import com.grupo03.solea.presentation.states.shared.AuthState
import com.grupo03.solea.presentation.states.shared.FormType
import com.grupo03.solea.utils.AuthError
import com.grupo03.solea.utils.AuthResult
import com.grupo03.solea.utils.CurrencyUtils
import com.grupo03.solea.utils.RepositoryResult
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
 * @property userRepository Repository for managing user profile data in Firestore
 */
class AuthViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : ViewModel() {

    private companion object {
        const val TAG = "AuthViewModel"
    }

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
        // Initialize currency with auto-detected value
        setSignUpForm {
            it.copy(currency = CurrencyUtils.getCurrencyByCountry())
        }
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
     * If a user is signed in, fetches their complete profile from Firestore including currency.
     * Called automatically during initialization and can be called manually to refresh state.
     */
    fun checkAuthState() {
        setLoading(true, null)
        viewModelScope.launch {
            val authUser = authService.getCurrentUser()

            if (authUser != null) {
                // User is signed in - fetch complete profile from Firestore
                val profileResult = userRepository.getUserProfile(authUser.uid)

                when (profileResult) {
                    is RepositoryResult.Success -> {
                        setUser(profileResult.data)
                        Log.d(TAG, "Loaded user profile from Firestore with currency: ${profileResult.data.currency}")
                    }
                    is RepositoryResult.Error -> {
                        // Profile doesn't exist - use auth user as fallback
                        setUser(authUser)
                        Log.w(TAG, "Firestore profile not found, using Firebase Auth user")
                    }
                }
            } else {
                // No user signed in
                setUser(null)
            }

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
     * Handles currency selection changes in the sign-up form.
     *
     * @param currencyCode The new currency code (ISO 4217, e.g., "USD", "PEN", "EUR")
     */
    fun onSignUpCurrencyChange(currencyCode: String) {
        setSignUpForm {
            it.copy(currency = currencyCode)
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
     * After successful Firebase Auth sign-in, fetches the complete user profile from Firestore
     * which includes the user's currency and other profile data.
     *
     * For existing users without Firestore profiles (migration scenario), creates a profile
     * using the currency stored in DataStore or auto-detected value.
     *
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

            val authResult = authService.signInWithEmailAndPassword(
                formState.email,
                formState.password
            )

            when (authResult) {
                is AuthResult.Success -> {
                    // Obtener perfil completo de Firestore
                    val profileResult = userRepository.getUserProfile(authResult.user.uid)

                    when (profileResult) {
                        is RepositoryResult.Success -> {
                            setUser(profileResult.data)
                            Log.d(TAG, "User signed in successfully with currency: ${profileResult.data.currency}")
                        }
                        is RepositoryResult.Error -> {
                            // Perfil no existe - usuario existente sin migración
                            // Usar el usuario de Auth sin currency (fallback a auto-detect en ViewModels)
                            setUser(authResult.user)
                            Log.w(TAG, "User profile not found in Firestore, using Auth user")
                        }
                    }
                }
                is AuthResult.Error -> {
                    setErrorCode(authResult.error)
                }
            }

            setLoading(false, FormType.SIGN_IN)
        }
    }

    /**
     * Performs sign-up with email and password.
     *
     * Validates all form fields (email, name, password, password confirmation, currency) before
     * attempting to create a new account. This is a two-phase process:
     * 1. Create Firebase Authentication account
     * 2. Create Firestore user profile with currency and other data
     *
     * If Firestore profile creation fails, the Firebase Auth user is deleted (rollback).
     * Updates the auth state with the new user on success or error on failure.
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

            // Paso 1: Crear usuario en Firebase Authentication
            val authResult = authService.signUpWithEmailAndPassword(
                email = formState.email,
                password = formState.password,
                displayName = formState.name,
                photoUrl = formState.photoUri
            )

            when (authResult) {
                is AuthResult.Success -> {
                    // Paso 2: Crear perfil en Firestore con currency
                    val user = authResult.user.copy(currency = formState.currency)
                    val profileResult = userRepository.createUserProfile(user)

                    when (profileResult) {
                        is RepositoryResult.Success -> {
                            setUser(profileResult.data)
                            Log.d(TAG, "User registered successfully with currency: ${formState.currency}")
                        }
                        is RepositoryResult.Error -> {
                            // Rollback: eliminar usuario de Firebase Auth si falla Firestore
                            authService.signOut()
                            setErrorCode(AuthError.UNKNOWN_ERROR)
                            Log.e(TAG, "Failed to create user profile in Firestore, user rolled back")
                        }
                    }
                }
                is AuthResult.Error -> {
                    setErrorCode(authResult.error)
                }
            }

            setLoading(false, FormType.SIGN_UP)
        }
    }

    /**
     * Performs sign-in with Google OAuth.
     *
     * Generates a Google sign-in request and attempts to authenticate using the Google
     * identity provider. After successful Google authentication, checks if the user has
     * a Firestore profile:
     * - Existing user: Fetches profile from Firestore
     * - New user: Creates profile with PEN currency by default
     *
     * Requires an Android context for the Google sign-in flow.
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

            val authResult = authService.signInWithGoogle(context, request)

            when (authResult) {
                is AuthResult.Success -> {
                    val authUser = authResult.user

                    // Verificar si ya existe perfil en Firestore
                    val profileResult = userRepository.getUserProfile(authUser.uid)

                    when (profileResult) {
                        is RepositoryResult.Success -> {
                            // Usuario existente
                            setUser(profileResult.data)
                            Log.d(TAG, "Google user signed in successfully with currency: ${profileResult.data.currency}")
                        }
                        is RepositoryResult.Error -> {
                            // Nuevo usuario de Google - crear perfil con PEN por defecto
                            val newUser = authUser.copy(currency = "PEN")
                            val createResult = userRepository.createUserProfile(newUser)

                            when (createResult) {
                                is RepositoryResult.Success -> {
                                    setUser(createResult.data)
                                    Log.d(TAG, "New Google user profile created with PEN currency")
                                }
                                is RepositoryResult.Error -> {
                                    // Si falla, usar user sin currency
                                    setUser(authUser)
                                    Log.e(TAG, "Failed to create Google user profile, using Auth user")
                                }
                            }
                        }
                    }
                }
                is AuthResult.Error -> {
                    setErrorCode(authResult.error)
                }
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
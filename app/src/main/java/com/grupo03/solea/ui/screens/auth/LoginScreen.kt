package com.grupo03.solea.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.grupo03.solea.R
import com.grupo03.solea.data.models.ServiceConstants
import com.grupo03.solea.presentation.states.AuthUiState
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.utils.ErrorCode
import com.grupo03.solea.utils.getStringRes
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    navigateToSignUp: () -> Unit,
    navigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navigateToHome()
        }
    }


    LoginForm(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = { pass -> viewModel.onPasswordChange(pass, true) },
        onLoginClick = viewModel::signInWithEmailAndPassword,
        onGoogleSignIn = viewModel::signInWithGoogle,
        onNavigateToSignUp = navigateToSignUp,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    uiState: AuthUiState = AuthUiState(),
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onGoogleSignIn: (String) -> Unit = {},
    onNavigateToSignUp: () -> Unit = { },
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var errorGoogle: ErrorCode.Auth? by remember {
        mutableStateOf(null)
    }

    var errorHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    fun handleGoogleSignIn() {
        coroutineScope.launch {
            try {
                Log.d("LoginForm", "Starting Google Sign-In")
                Log.d("LoginForm", "Package name: ${context.packageName}")
                Log.d("LoginForm", "WEB_CLIENT_ID: ${ServiceConstants.WEB_CLIENT_ID}")

                val credentialManager = CredentialManager.create(context)
                Log.d("LoginForm", "CredentialManager created successfully")

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(ServiceConstants.WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .setNonce(null)
                    .build()

                Log.d(
                    "LoginForm",
                    "GoogleIdOption built with server client ID: ${ServiceConstants.WEB_CLIENT_ID}"
                )

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Log.d("LoginForm", "Requesting credential...")

                val result = credentialManager.getCredential(
                    request = request,
                    context = context as Activity
                )

                Log.d("LoginForm", "Credential obtained successfully")
                Log.d("LoginForm", "Credential type: ${result.credential.type}")
                Log.d("LoginForm", "Credential class: ${result.credential::class.java.simpleName}")

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    Log.d("LoginForm", "Valid Google credential found, parsing token...")
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    Log.d("LoginForm", "Token parsed successfully, calling onGoogleSignIn")
                    onGoogleSignIn(googleIdTokenCredential.idToken)
                } else {
                    Log.e("LoginForm", "Invalid credential type received: ${credential.type}")
                    Log.e(
                        "LoginForm",
                        "Expected: ${GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL}"
                    )
                    errorGoogle = ErrorCode.Auth.GOOGLE_SIGN_IN_FAILED
                }
            } catch (e: Exception) {
                Log.e(
                    "LoginForm",
                    "Google Sign-In failed with exception: ${e::class.java.simpleName}"
                )
                Log.e("LoginForm", "Error message: ${e.message}")
                Log.e("LoginForm", "Stack trace: ", e)
                errorGoogle = ErrorCode.Auth.GOOGLE_SIGN_IN_FAILED
            }
        }
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )
        Column(
            modifier = Modifier.weight(3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.solea),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .weight(0.4f),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .padding(bottom = 40.dp, start = 10.dp)
                        .weight(0.6f),
                    textAlign = TextAlign.Left
                )
            }
            // Email Input Field
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email_label)) },
                isError = !uiState.isEmailValid || uiState.errorCode != null
            )
            // Password Input Field
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password_label)) },
                isError = uiState.errorCode != null
            )


            Box(
                modifier = Modifier
                    .height(if (uiState.errorCode != null || errorGoogle != null) errorHeight else 30.dp)
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                if (uiState.errorCode != null || errorGoogle != null) {
                    val errorCode = uiState.errorCode ?: errorGoogle
                    Text(
                        text = stringResource(errorCode!!.getStringRes()),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                errorHeight = with(density) {
                                    coordinates.size.height.toDp() + 30.dp // + padding
                                }
                            }
                    )
                }
            }

            Button(
                onClick = onLoginClick,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                Text(if (!uiState.isLoading) stringResource(R.string.login_button_label) else "Loading...")
            }

            OutlinedButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.no_accound_sign_up_label))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = stringResource(R.string.or),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                )
            }
            OutlinedButton(
                onClick = ::handleGoogleSignIn,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.google_brand),
                        contentDescription = "Google Icon",
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(text = stringResource(R.string.button_sign_in_google))
                }
            }
        }
        Spacer(
            modifier = Modifier.weight(1f)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun LoginFormPreview() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface {
            LoginForm(
                modifier = Modifier.fillMaxSize(),
                uiState = AuthUiState(
                    email = "example@gmail.com",
                    password = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    isLoading = false,
                    isLoggedIn = false,
                    errorCode = null,
                ),
            )
        }
    }
}



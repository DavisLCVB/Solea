package com.grupo03.solea.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.presentation.states.AuthUiState
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.utils.ErrorCode
import com.grupo03.solea.utils.getStringRes

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
        onLoginClick = viewModel::signIn,
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
    onNavigateToSignUp: () -> Unit = { },
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )
        Column(
            modifier = Modifier.weight(3f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 40.dp),
                textAlign = TextAlign.Center
            )
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
            Button(
                onClick = onLoginClick,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
            ) {
                Text(if (!uiState.isLoading) stringResource(R.string.login_button_label) else "Loading...")
            }
            OutlinedButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.no_accound_sign_up_label))
            }
            if (uiState.errorCode != null) {
                Text(
                    text = stringResource(uiState.errorCode.getStringRes()),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
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
    SoleaTheme {
        LoginForm(
            modifier = Modifier.fillMaxSize(),
            uiState = AuthUiState(
                email = "example@gmail.com",
                password = "password123",
                isEmailValid = true,
                isPasswordValid = true,
                isLoading = false,
                isLoggedIn = false,
                errorCode = ErrorCode.Auth.USER_COLLISION,
            ),
        )
    }
}



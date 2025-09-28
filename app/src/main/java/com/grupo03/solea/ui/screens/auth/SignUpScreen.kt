package com.grupo03.solea.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.presentation.states.AuthState
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.utils.ErrorCode
import com.grupo03.solea.utils.getStringRes

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    navigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState = uiState.signUpFormState
    val context = LocalContext.current

    fun onEmailChange(newEmail: String) {
        viewModel.onEmailChange(AuthState.FormType.REGISTER, newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        viewModel.onPasswordChange(AuthState.FormType.REGISTER, newPassword)
    }

    fun onGoogleSignUp() {
        viewModel.signInWithGoogle(context)
    }

    SignUpForm(
        formState = formState,
        onNameChange = viewModel::onSignUpNameChange,
        onEmailChange = ::onEmailChange,
        onPasswordChange = ::onPasswordChange,
        onConfirmPasswordChange = viewModel::onSignUpConfirmPasswordChange,
        onSignUpClick = viewModel::signUpWithEmailAndPassword,
        onNavigateToLogin = navigateToLogin,
        modifier = Modifier.fillMaxSize(),
        errorCode = uiState.errorCode,
        isLoading = uiState.isLoading,
        onGoogleSignUp = ::onGoogleSignUp
    )
}

@Composable
fun SignUpForm(
    modifier: Modifier = Modifier,
    formState: AuthState.SignUpFormState = AuthState.SignUpFormState(),
    onEmailChange: (String) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onGoogleSignUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    errorCode: ErrorCode.Auth? = null,
    isLoading: Boolean = false,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.solea_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .weight(0.4f),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .padding(bottom = 20.dp, start = 10.dp)
                        .weight(0.6f),
                    textAlign = TextAlign.Left
                )
            }
            Text(
                text = stringResource(R.string.create_your_account),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = formState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name_label)) },
                isError = !formState.isEmailValid || errorCode != null,
                modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
            )
            OutlinedTextField(
                value = formState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email_label)) },
                isError = !formState.isEmailValid || errorCode != null,
                modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
            )
            // Password Input Field
            OutlinedTextField(
                value = formState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password_label)) },
                isError = !formState.isPasswordValid || errorCode != null,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
            )
            OutlinedTextField(
                value = formState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text(stringResource(R.string.confirm_password_label)) },
                isError = !formState.isPasswordValid || errorCode != null,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
            )
            Button(
                onClick = onSignUpClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
            ) {
                Text(stringResource(R.string.sign_up_button))
            }
            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.already_have_an_account_log_in_button),
                    textAlign = TextAlign.Center
                )
            }
            if (errorCode != null) {
                Text(
                    text = stringResource(errorCode.getStringRes()),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
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
                onClick = onGoogleSignUp,
                enabled = !isLoading,
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
fun SignUpFormPreview() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface {
            SignUpForm(
                modifier = Modifier.fillMaxSize(),
                formState = AuthState.SignUpFormState(
                    name = "Hello",
                    email = "example@gmail.com",
                    password = "password123",
                    confirmPassword = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                ),
            )
        }
    }

}
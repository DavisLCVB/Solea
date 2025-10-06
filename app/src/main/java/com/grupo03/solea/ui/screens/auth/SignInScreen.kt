package com.grupo03.solea.ui.screens.auth

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.presentation.states.screens.SignInFormState
import com.grupo03.solea.presentation.states.shared.FormType
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.utils.AuthError
import com.grupo03.solea.utils.getStringRes

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    navigateToSignUp: () -> Unit,
) {
    val formState by viewModel.signInFormState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    fun onEmailChange(newEmail: String) {
        viewModel.onEmailChange(FormType.SIGN_IN, newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        viewModel.onPasswordChange(FormType.SIGN_IN, newPassword)
    }

    fun onSignInWithGoogle() {
        viewModel.signInWithGoogle(context)
    }


    SignInForm(
        formState = formState,
        onEmailChange = ::onEmailChange,
        onPasswordChange = ::onPasswordChange,
        onLoginClick = viewModel::signInWithEmailAndPassword,
        onGoogleSignIn = ::onSignInWithGoogle,
        onNavigateToSignUp = navigateToSignUp,
        modifier = Modifier.fillMaxSize(),
        errorCode = authState.errorCode,
        isLoading = formState.isLoading,
    )
}

@Composable
fun SignInForm(
    modifier: Modifier = Modifier,
    formState: SignInFormState = SignInFormState(),
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = { },
    errorCode: AuthError? = null,
    isLoading: Boolean = false,
) {
    var errorGoogle: AuthError? by remember {
        mutableStateOf(null)
    }
    var errorHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.weight(3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo y título
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.solea_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .weight(0.4f),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .padding(bottom = 8.dp, start = 10.dp)
                        .weight(0.6f),
                    textAlign = TextAlign.Left
                )
            }

            // Título de bienvenida
            Text(
                text = stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.sign_in_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            // Email Input Field
            OutlinedTextField(
                value = formState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email_label)) },
                isError = !formState.isEmailValid || errorCode != null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Password Input Field
            OutlinedTextField(
                value = formState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password_label)) },
                isError = errorCode != null,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .height(if (errorCode != null || errorGoogle != null) errorHeight else 24.dp)
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                if (errorCode != null || errorGoogle != null) {
                    val errorCode = errorCode ?: errorGoogle
                    Text(
                        text = stringResource(errorCode!!.getStringRes()),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                errorHeight = with(density) {
                                    coordinates.size.height.toDp() + 24.dp
                                }
                            }
                    )
                }
            }

            Button(
                onClick = onLoginClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(
                    text = if (!isLoading) stringResource(R.string.login_button_label) else stringResource(
                        R.string.loading
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            OutlinedButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_accound_sign_up_label),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = stringResource(R.string.or),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                )
            }

            OutlinedButton(
                onClick = onGoogleSignIn,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    Text(
                        text = stringResource(R.string.button_sign_in_google),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}


@Preview(showBackground = true)
@Composable
fun SignInFormPreview() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface {
            SignInForm(
                modifier = Modifier.fillMaxSize(),
                formState = SignInFormState(
                    email = "test@email.com",
                    password = "password123",
                    isEmailValid = true
                ),
            )
        }
    }
}



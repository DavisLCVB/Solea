package com.grupo03.solea.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.grupo03.solea.R
import com.grupo03.solea.presentation.states.screens.SignUpFormState
import com.grupo03.solea.presentation.states.shared.FormType
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.utils.AuthError
import com.grupo03.solea.utils.CurrencyUtils
import com.grupo03.solea.utils.getStringRes

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    navigateToLogin: () -> Unit,
) {
    val authState by viewModel.authState.collectAsState()
    val formState by viewModel.signUpFormState.collectAsState()
    val context = LocalContext.current

    fun onEmailChange(newEmail: String) {
        viewModel.onEmailChange(FormType.SIGN_UP, newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        viewModel.onPasswordChange(FormType.SIGN_UP, newPassword)
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
        onCurrencyChange = viewModel::onSignUpCurrencyChange,
        onPhotoChange = viewModel::onSignUpPhotoChange,
        onSignUpClick = viewModel::signUpWithEmailAndPassword,
        onNavigateToLogin = navigateToLogin,
        modifier = Modifier.fillMaxSize(),
        errorCode = authState.errorCode,
        isLoading = formState.isLoading,
        onGoogleSignUp = ::onGoogleSignUp
    )
}

@Composable
fun SignUpForm(
    modifier: Modifier = Modifier,
    formState: SignUpFormState = SignUpFormState(),
    onEmailChange: (String) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onCurrencyChange: (String) -> Unit = {},
    onPhotoChange: (String?) -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onGoogleSignUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    errorCode: AuthError? = null,
    isLoading: Boolean = false,
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onPhotoChange(uri?.toString())
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.weight(3f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .zIndex(10f)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            )
                        ),
                        shape = RectangleShape
                    )
            )
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(6f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo y título
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, top = 24.dp),
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

                    Text(
                        text = stringResource(R.string.create_your_account),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.sign_up_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    // Profile Photo Picker
                    Box(
                        modifier = Modifier
                            .size(100.dp) // Asegura ancho y alto iguales
                            .clip(CircleShape) // Recorta primero
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (formState.photoUri != null) {
                            AsyncImage(
                                model = formState.photoUri,
                                contentDescription = stringResource(R.string.profile_photo),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = stringResource(R.string.add_photo),
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_photo),
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }


                    Text(
                        text = stringResource(R.string.tap_to_add_photo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = formState.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.display_name_label)) },
                        isError = !formState.isNameValid || errorCode != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = formState.email,
                        onValueChange = onEmailChange,
                        label = { Text(stringResource(R.string.email_label)) },
                        isError = !formState.isEmailValid || errorCode != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = formState.password,
                        onValueChange = onPasswordChange,
                        label = { Text(stringResource(R.string.password_label)) },
                        isError = !formState.isPasswordValid || errorCode != null,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = formState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text(stringResource(R.string.confirm_password_label)) },
                        isError = !formState.isPasswordValid || errorCode != null,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.padding(top = 16.dp))

                    // Currency selector
                    Text(
                        text = stringResource(R.string.currency_label),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CurrencyDropdown(
                        selectedCurrency = formState.currency,
                        onCurrencyChange = onCurrencyChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(R.string.currency_permanent_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    if (errorCode != null) {
                        Text(
                            text = stringResource(errorCode.getStringRes()),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Button(
                        onClick = onSignUpClick,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                    ) {
                        Text(
                            text = if (!isLoading) stringResource(R.string.sign_up_button) else stringResource(
                                R.string.loading
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.already_have_an_account_log_in_button),
                            textAlign = TextAlign.Center,
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
                        onClick = onGoogleSignUp,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 25.dp),
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
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier.heightIn(min = 100.dp)
                    )
                }


            }

            // Bottom fade shader
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        ),
                        shape = RectangleShape
                    )
            )

        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CurrencyDropdown(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currencies = CurrencyUtils.getSupportedCurrencies()

    OutlinedButton(
        onClick = { expanded = true },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCurrency.isNotEmpty()) {
                    "${CurrencyUtils.getCurrencySymbol(selectedCurrency)} $selectedCurrency"
                } else {
                    stringResource(R.string.select_currency)
                }
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        currencies.forEach { currency ->
            DropdownMenuItem(
                text = {
                    Text("${currency.symbol} ${currency.code} - ${currency.name}")
                },
                onClick = {
                    onCurrencyChange(currency.code)
                    expanded = false
                },
                leadingIcon = {
                    if (currency.code == selectedCurrency) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
        }
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
                formState = SignUpFormState(
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

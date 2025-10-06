package com.grupo03.solea.ui.screens.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.presentation.viewmodels.screens.NewCategoryFormViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.ui.theme.SoleaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCategoryFormScreen(
    newCategoryFormViewModel: NewCategoryFormViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val formState = newCategoryFormViewModel.formState.collectAsState()
    val userId = authViewModel.authState.collectAsState().value.user!!.uid
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Show error message
    LaunchedEffect(formState.value.error) {
        formState.value.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = context.getString(error.messageRes)
            )
        }
    }

    // Show success message
    LaunchedEffect(formState.value.successMessage) {
        formState.value.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.grupo03.solea.R.string.new_category)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(com.grupo03.solea.R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(com.grupo03.solea.R.string.category_creation_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name field
            OutlinedTextField(
                value = formState.value.name,
                onValueChange = newCategoryFormViewModel::onNameChange,
                label = { Text(stringResource(com.grupo03.solea.R.string.category_name)) },
                placeholder = { Text(stringResource(com.grupo03.solea.R.string.category_example)) },
                supportingText = {
                    if (!formState.value.isNameValid) {
                        Text(
                            text = stringResource(com.grupo03.solea.R.string.name_min_3_chars),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = !formState.value.isNameValid,
                enabled = !formState.value.isLoading,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description field
            OutlinedTextField(
                value = formState.value.description,
                onValueChange = newCategoryFormViewModel::onDescriptionChange,
                label = { Text(stringResource(com.grupo03.solea.R.string.description_optional_label)) },
                placeholder = { Text(stringResource(com.grupo03.solea.R.string.category_description)) },
                enabled = !formState.value.isLoading,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = {
                        newCategoryFormViewModel.clearForm()
                        onNavigateBack()
                    },
                    enabled = !formState.value.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(com.grupo03.solea.R.string.cancel_button),
                        fontSize = 16.sp
                    )
                }

                // Submit button
                Button(
                    onClick = {
                        newCategoryFormViewModel.createCategory(userId) {
                            newCategoryFormViewModel.clearForm()
                            onNavigateBack()
                        }
                    },
                    enabled = !formState.value.isLoading && formState.value.isNameValid && formState.value.name.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    if (formState.value.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = if (formState.value.isLoading) stringResource(com.grupo03.solea.R.string.creating_button) else stringResource(com.grupo03.solea.R.string.create_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewCategoryFormScreenPreview() {
    SoleaTheme(darkTheme = true) {
        NewCategoryFormScreenContent(
            formState = com.grupo03.solea.presentation.states.screens.NewCategoryFormState(
                name = "Comida",
                description = "Gastos de alimentación"
            ),
            onNameChange = {},
            onDescriptionChange = {},
            onSubmit = {},
            onCancel = {},
            isLoading = false
        )
    }
}

@Composable
private fun NewCategoryFormScreenContent(
    formState: com.grupo03.solea.presentation.states.screens.NewCategoryFormState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create a new category to organize your movements",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.name,
            onValueChange = onNameChange,
            label = { Text("Category name") },
            placeholder = { Text("e.g.: Food, Transportation, Entertainment") },
            supportingText = {
                if (!formState.isNameValid) {
                    Text(
                        text = "Name must be at least 3 characters",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = !formState.isNameValid,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = formState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (optional)") },
            placeholder = { Text("Add a description for this category") },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = onSubmit,
                enabled = !isLoading && formState.isNameValid && formState.name.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = if (isLoading) "Creating..." else "Create",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.grupo03.solea.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.grupo03.solea.R
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.ui.components.SectionTitle
import com.grupo03.solea.ui.components.TopBar

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: com.grupo03.solea.presentation.viewmodels.screens.SettingsViewModel,
    onNavigateToBudgetLimits: () -> Unit,
    onNavigateToLanguageSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState = authViewModel.authState.collectAsState()
    val settingsState = settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    SettingsContent(
        user = authState.value.user,
        settingsState = settingsState.value,
        onToggleNotifications = settingsViewModel::toggleNotifications,
        onToggleTheme = settingsViewModel::toggleTheme,
        onSignOut = { authViewModel.signOut() },
        onNavigateToBudgetLimits = onNavigateToBudgetLimits,
        onNavigateToLanguageSelection = onNavigateToLanguageSelection,
        onExportExcel = {
            authState.value.user?.uid?.let { userUid ->
                settingsViewModel.exportMovementsToExcel(context, userUid)
            }
        },
        onExportPdf = {
            authState.value.user?.uid?.let { userUid ->
                settingsViewModel.exportMovementsToPdf(context, userUid)
            }
        },
        onClearExportSuccess = settingsViewModel::clearExportSuccess,
        onClearError = settingsViewModel::clearError,
        modifier = modifier
    )
}


@Composable
private fun SettingsContent(
    user: com.grupo03.solea.data.models.User?,
    settingsState: com.grupo03.solea.presentation.states.screens.SettingsState,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToBudgetLimits: () -> Unit,
    onNavigateToLanguageSelection: () -> Unit,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit,
    onClearExportSuccess: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName = user?.displayName ?: "Usuario"
    val userEmail = user?.email ?: "correo@ejemplo.com"
    val isGoogleUser = user?.photoUrl?.contains("googleusercontent.com") == true
    val userCurrency = user?.currency ?: "PEN"

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success message
    LaunchedEffect(settingsState.exportSuccess) {
        settingsState.exportSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearExportSuccess()
        }
    }

    // Show error message
    LaunchedEffect(settingsState.error) {
        settingsState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(title = stringResource(R.string.configuration))

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            UserProfileCard(
                userName = userName,
                userEmail = userEmail,
                photoUrl = user?.photoUrl,
                isGoogleUser = isGoogleUser,
                userCurrency = userCurrency,
                onEditProfile = { }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Gastos Section
            SectionTitle(
                text = stringResource(R.string.expenses_configuration),
                icon = Icons.Default.Wallet
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingItemWithSwitch(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.expenses_notifications),
                        checked = settingsState.notificationsEnabled,
                        onCheckedChange = onToggleNotifications
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingNavigationCard(
                icon = Icons.Default.Receipt,
                title = stringResource(R.string.limits_per_category_stablish),
                onClick = onNavigateToBudgetLimits
            )

            Spacer(modifier = Modifier.height(20.dp))

            // General Section
            SectionTitle(text = "General", icon = Icons.Default.Language)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingItemWithSwitch(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.dark_theme),
                        checked = settingsState.isDarkTheme,
                        onCheckedChange = onToggleTheme
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingNavigationCard(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language_selection_title),
                onClick = onNavigateToLanguageSelection
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val context = LocalContext.current
                    val appVersion = try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    } catch (_: Exception) {
                        "1.0.0"
                    }
                    val appVersionPrefix = stringResource(R.string.version_prefix)
                    Text(
                        text = stringResource(R.string.about_app),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$appVersionPrefix $appVersion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Export Data Section
            SectionTitle(text = stringResource(R.string.export_data), icon = Icons.Default.FileDownload)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.export_movements_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onExportExcel,
                            enabled = !settingsState.isExporting,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (settingsState.isExporting) {
                                Text(stringResource(R.string.exporting))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.export_excel))
                            }
                        }

                        Button(
                            onClick = onExportPdf,
                            enabled = !settingsState.isExporting,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (settingsState.isExporting) {
                                Text(stringResource(R.string.exporting))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.export_pdf))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            Button(
                onClick = onSignOut,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.sign_out))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun UserProfileCard(
    userName: String,
    userEmail: String,
    photoUrl: String?,
    isGoogleUser: Boolean,
    userCurrency: String,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Picture
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = stringResource(R.string.profile_photo),
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.avatar_description),
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isGoogleUser) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.google_account),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Currency info (read-only)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.currency_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${com.grupo03.solea.utils.CurrencyUtils.getCurrencySymbol(userCurrency)} $userCurrency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Edit Profile Button
            Button(
                onClick = onEditProfile,
                enabled = !isGoogleUser,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isGoogleUser) stringResource(R.string.cannot_edit_google_profile) else stringResource(
                        R.string.edit_profile
                    )
                )
            }
        }
    }
}


@Composable
private fun SettingItemWithSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingNavigationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navegar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

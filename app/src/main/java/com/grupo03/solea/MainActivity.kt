package com.grupo03.solea

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.grupo03.solea.presentation.viewmodels.screens.AudioAnalysisViewModel
import com.grupo03.solea.presentation.viewmodels.screens.BudgetViewModel
import com.grupo03.solea.presentation.viewmodels.screens.ScanReceiptViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel
import com.grupo03.solea.ui.components.BottomNavigationBar
import com.grupo03.solea.ui.navigation.AppRoutes
import com.grupo03.solea.ui.navigation.AuthRoutes
import com.grupo03.solea.ui.navigation.authNavigationGraph
import com.grupo03.solea.ui.navigation.mainNavigationGraph
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.utils.LocaleUtils
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        // Apply language preference on cold start
        // Read from DataStore preferences to ensure correct language before any UI is rendered
        val prefs = newBase.getSharedPreferences("datastore", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "es") ?: "es"
        super.attachBaseContext(LocaleUtils.setLocale(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val settingsViewModel: com.grupo03.solea.presentation.viewmodels.screens.SettingsViewModel = koinViewModel()
            val settingsState = settingsViewModel.uiState.collectAsState()

            // Create locale-specific context for instant language switching
            val currentLanguage = settingsState.value.selectedLanguage
            val localeContext = remember(currentLanguage) {
                LocaleUtils.setLocale(this@MainActivity, currentLanguage)
            }

            // Provide locale context to entire app - this enables instant language switching
            CompositionLocalProvider(LocalContext provides localeContext) {
                SoleaTheme(darkTheme = settingsState.value.isDarkTheme) {
                    val isDarkTheme = settingsState.value.isDarkTheme

                    SideEffect {
                        WindowCompat.getInsetsController(window, window.decorView).apply {
                            isAppearanceLightStatusBars = !isDarkTheme
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authState = authViewModel.authState.collectAsState()
    val movementsViewModel: MovementsViewModel = koinViewModel()
    val budgetViewModel: BudgetViewModel =
        koinViewModel()

    if (authState.value.user == null) {
        NavHost(
            navController = navController,
            startDestination = AuthRoutes.PREFIX
        ) {
            authNavigationGraph(navController, authViewModel)
        }
    } else {
        movementsViewModel.fetchMovements(userId = authState.value.user!!.uid)
        MainAppContent(
            navController = navController,
            authViewModel = authViewModel,
            budgetViewModel = budgetViewModel,
            movementsViewModel = movementsViewModel
        )
    }
}

@Composable
fun MainAppContent(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    budgetViewModel: BudgetViewModel,
    movementsViewModel: MovementsViewModel
) {
    val scanReceiptViewModel: ScanReceiptViewModel = koinViewModel()
    val audioAnalysisViewModel: AudioAnalysisViewModel = koinViewModel()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.PREFIX,
            modifier = Modifier.padding(paddingValues)
        ) {
            mainNavigationGraph(
                navController = navController,
                authViewModel = authViewModel,
                budgetViewModel = budgetViewModel,
                movementsViewModel = movementsViewModel,
                scanReceiptViewModel = scanReceiptViewModel,
                audioAnalysisViewModel = audioAnalysisViewModel,
                contentPadding = PaddingValues(0.dp)
            )

        }
    }
}
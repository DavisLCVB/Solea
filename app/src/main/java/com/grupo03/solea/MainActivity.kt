package com.grupo03.solea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.grupo03.solea.data.repositories.FirebaseAuthRepository
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.ui.navigation.AppRoutes
import com.grupo03.solea.ui.navigation.AuthRoutes
import com.grupo03.solea.ui.navigation.authNavigationGraph
import com.grupo03.solea.ui.navigation.mainNavigationGraph
import com.grupo03.solea.ui.theme.SoleaTheme

class MainActivity : ComponentActivity() {
    private val authRepository = FirebaseAuthRepository(Firebase.auth)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoleaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(authRepository)
                }
            }
        }
    }
}


@Composable
fun AppNavigation(
    authRepository: FirebaseAuthRepository
) {
    val navController = rememberNavController()
    val authViewModel = viewModel {
        AuthViewModel(authRepository)
    }
    val authState = authViewModel.uiState.collectAsState()

    val startDestination = remember(authState.value.isLoggedIn) {
        if (authState.value.isLoggedIn) AppRoutes.PREFIX else AuthRoutes.PREFIX
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authNavigationGraph(navController, authViewModel)
        mainNavigationGraph(authViewModel)
    }
}
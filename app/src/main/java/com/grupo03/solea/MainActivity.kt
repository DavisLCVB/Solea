package com.grupo03.solea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.grupo03.solea.data.repositories.FirebaseMovementsRepository
import com.grupo03.solea.data.repositories.MovementsRepository
import com.grupo03.solea.data.services.AuthService
import com.grupo03.solea.data.services.FirebaseAuthService
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.MovementsViewModel
import com.grupo03.solea.ui.navigation.AppRoutes
import com.grupo03.solea.ui.navigation.AuthRoutes
import com.grupo03.solea.ui.navigation.BottomNavigationBar
import com.grupo03.solea.ui.navigation.authNavigationGraph
import com.grupo03.solea.ui.navigation.mainNavigationGraph
import com.grupo03.solea.ui.theme.SoleaTheme

class MainActivity : ComponentActivity() {
    private val authService = FirebaseAuthService(Firebase.auth)
    private val movementsRepository = FirebaseMovementsRepository(Firebase.firestore)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoleaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(authService, movementsRepository)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    authService: AuthService,
    movementsRepository: MovementsRepository
) {
    val navController = rememberNavController()
    val authViewModel = viewModel {
        AuthViewModel(authService)
    }
    val authState = authViewModel.uiState.collectAsState()
    val movementsViewModel = viewModel {
        MovementsViewModel(movementsRepository)
    }

    val startDestination = remember(authState.value.user) {
        if (authState.value.user != null) AppRoutes.PREFIX else AuthRoutes.PREFIX
    }

    var selectedBottomItem by remember { mutableStateOf<Int>(2) } // "Inicio" selected (index 2)

    if (authState.value.user == null) {
        NavHost(
            navController = navController,
            startDestination = AuthRoutes.PREFIX
        ) {
            authNavigationGraph(navController, authViewModel)
        }
    } else {
        MainAppContent(
            navController = navController,
            authViewModel = authViewModel,
            movementsViewModel = movementsViewModel
        )
    }
}

@Composable
fun MainAppContent(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    movementsViewModel: MovementsViewModel
) {
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
                authViewModel = authViewModel,
                movementsViewModel = movementsViewModel,
                navController = navController,
                contentPadding = PaddingValues(0.dp)
            )
        }
    }
}
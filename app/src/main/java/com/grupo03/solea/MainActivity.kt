package com.grupo03.solea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.grupo03.solea.ui.screens.home.BottomNavigationBar
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


@OptIn(ExperimentalMaterial3Api::class)
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

    var selectedBottomItem by remember { mutableStateOf<Int>(2) } // "Inicio" selected (index 2)

    if (!authState.value.isLoggedIn) {
        // Solo flujo de autenticación, sin barras
        NavHost(
            navController = navController,
            startDestination = AuthRoutes.PREFIX
        ) {
            authNavigationGraph(navController, authViewModel)
        }
    } else {
        // Navegación principal con barras
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedBottomItem) {
                                0 -> "Perfil / Configuración"
                                1 -> "Ahorro"
                                2 -> "Solea"
                                3 -> "Historial"
                                4 -> "Lista de compras"
                                else -> "Solea"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    actions = {
                        IconButton(onClick = { authViewModel.signOut() }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                com.grupo03.solea.ui.screens.home.BottomNavigationBar(
                    selectedItem = selectedBottomItem,
                    onItemSelected = {
                        selectedBottomItem = it
                        when (it) {
                            0 -> navController.navigate(AppRoutes.SETTINGS)
                            1 -> navController.navigate(AppRoutes.SAVINGS)
                            2 -> navController.navigate(AppRoutes.HOME)
                            3 -> navController.navigate(AppRoutes.HISTORY)
                            4 -> navController.navigate(AppRoutes.SHOPPING_LIST)
                        }
                    }
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
                    navController = navController,
                    contentPadding = PaddingValues(0.dp)
                )
            }
        }
    }
}
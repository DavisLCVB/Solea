package com.grupo03.solea.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.ui.screens.auth.LoginScreen
import com.grupo03.solea.ui.screens.auth.SignUpScreen

fun NavGraphBuilder.authNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = Routes.Auth.LOGIN,
        route = Routes.Auth.BASE
    ) {
        composable(Routes.Auth.LOGIN) {

            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(Routes.Auth.SIGN_UP)
                },
                onNavigateToHome = {
                    navController.navigate(Routes.Home.HOME) {
                        popUpTo(Routes.Auth.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Routes.Auth.SIGN_UP) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.Auth.LOGIN) {
                        popUpTo(Routes.Auth.LOGIN) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.Home.HOME) {
                        popUpTo(Routes.Auth.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
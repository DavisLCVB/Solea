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
        startDestination = AuthRoutes.LOGIN,
        route = AuthRoutes.PREFIX
    ) {
        composable(AuthRoutes.LOGIN) {

            LoginScreen(
                viewModel = authViewModel,
                navigateToSignUp = {
                    navController.navigate(AuthRoutes.SIGN_UP)
                },
                navigateToHome = {
                    navController.navigate(AppRoutes.PREFIX) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AuthRoutes.SIGN_UP) {
            SignUpScreen(
                viewModel = authViewModel,
                navigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.LOGIN) {
                            inclusive = true
                        }
                    }
                },
                navigateToHome = {
                    navController.navigate(AppRoutes.PREFIX) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
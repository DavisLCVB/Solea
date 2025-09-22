package com.grupo03.solea.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.ui.screens.home.HomeScreen

fun NavGraphBuilder.mainNavigationGraph(
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = AppRoutes.HOME,
        route = AppRoutes.PREFIX
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(authViewModel)
        }
    }
}
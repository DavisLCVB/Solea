package com.grupo03.solea.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.grupo03.solea.ui.screens.home.HomeScreen

fun NavGraphBuilder.mainNavigationGraph() {
    navigation(
        startDestination = Routes.Home.HOME,
        route = Routes.Home.BASE
    ) {
        composable(Routes.Home.HOME) {
            HomeScreen()
        }
    }
}
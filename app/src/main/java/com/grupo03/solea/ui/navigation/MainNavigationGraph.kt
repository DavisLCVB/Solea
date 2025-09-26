package com.grupo03.solea.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.ui.screens.home.HomeScreen

import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.grupo03.solea.ui.screens.home.HomeScreenContent

fun NavGraphBuilder.mainNavigationGraph(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    navigation(
        startDestination = AppRoutes.HOME,
        route = AppRoutes.PREFIX
    ) {
        composable(AppRoutes.HOME) {
            HomeScreenContent(
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.HISTORY) {
            com.grupo03.solea.ui.screens.history.HistoryScreen(
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.SAVINGS) {
            com.grupo03.solea.ui.screens.savings.SavingsScreen(
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.SHOPPING_LIST) {
            com.grupo03.solea.ui.screens.shoppinglist.ShoppingListScreen(
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.SETTINGS) {
            com.grupo03.solea.ui.screens.settings.SettingsScreen(
                authViewModel = authViewModel,
                modifier = Modifier.padding(contentPadding)
            )
        }
    }
}
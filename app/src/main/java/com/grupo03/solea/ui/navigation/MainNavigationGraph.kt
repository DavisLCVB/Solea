package com.grupo03.solea.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.grupo03.solea.presentation.states.CoreState
import com.grupo03.solea.presentation.viewmodels.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.CoreViewModel
import com.grupo03.solea.ui.screens.home.HomeScreen

fun NavGraphBuilder.mainNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    coreViewModel: CoreViewModel,
    contentPadding: PaddingValues
) {
    navigation(
        startDestination = AppRoutes.HOME,
        route = AppRoutes.PREFIX
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                authViewModel = authViewModel,
                coreViewModel = coreViewModel
            )
        }
        composable(AppRoutes.HISTORY) {
            com.grupo03.solea.ui.screens.history.HistoryScreen(
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.SAVINGS) {
            com.grupo03.solea.ui.screens.savings.SavingsScreen(
                authViewModel = authViewModel,
                coreViewModel = coreViewModel,
                onNavigateToBudgetLimits = {
                    coreViewModel.changeSettingsContent(CoreState.SettingsContent.BUDGET_LIMITS)
                    navController.navigate(AppRoutes.SETTINGS) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onEditBudget = { category ->
                    coreViewModel.onSelectCategoryForBudget(category)
                    coreViewModel.changeSettingsContent(CoreState.SettingsContent.BUDGET_LIMITS)
                    navController.navigate(AppRoutes.SETTINGS) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
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
                coreViewModel = coreViewModel,  // AGREGAR ESTE PARÁMETRO
                modifier = Modifier.padding(contentPadding)
            )
        }
    }
}
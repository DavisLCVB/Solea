package com.grupo03.solea.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.grupo03.solea.presentation.viewmodels.screens.BudgetViewModel
import com.grupo03.solea.presentation.viewmodels.screens.ScanReceiptViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel
import com.grupo03.solea.ui.screens.forms.NewCategoryFormScreen
import com.grupo03.solea.ui.screens.forms.NewMovementFormScreen
import com.grupo03.solea.ui.screens.history.HistoryScreen
import com.grupo03.solea.ui.screens.home.HomeScreen
import com.grupo03.solea.ui.screens.scanner.EditScannedReceiptScreen
import com.grupo03.solea.ui.screens.scanner.ScanReceiptScreen
import com.grupo03.solea.ui.screens.settings.BudgetLimitsScreen
import com.grupo03.solea.ui.screens.settings.EditBudgetForm
import com.grupo03.solea.ui.screens.settings.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.mainNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    budgetViewModel: BudgetViewModel,
    movementsViewModel: MovementsViewModel,
    scanReceiptViewModel: ScanReceiptViewModel,
    contentPadding: PaddingValues,
) {
    navigation(
        startDestination = AppRoutes.HOME,
        route = AppRoutes.PREFIX
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                homeViewModel = koinViewModel(),
                movementsViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateToNewMovement = {
                    navController.navigate(AppRoutes.NEW_MOVEMENT)
                },
                onNavigateToNewCategory = {
                    navController.navigate(AppRoutes.NEW_CATEGORY)
                },
                onNavigateToScanReceipt = {
                    navController.navigate(AppRoutes.SCAN_RECEIPT)
                }
            )
        }
        composable(AppRoutes.NEW_CATEGORY) {
            NewCategoryFormScreen(
                newCategoryFormViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppRoutes.NEW_MOVEMENT) {
            NewMovementFormScreen(
                newMovementFormViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNewCategory = {
                    navController.navigate(AppRoutes.NEW_CATEGORY)
                }
            )
        }
        composable(AppRoutes.HISTORY) {
            HistoryScreen(
                historyViewModel = koinViewModel(),
                authViewModel = authViewModel,
                movementsViewModel = movementsViewModel,
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.SAVINGS) {
            com.grupo03.solea.ui.screens.savings.SavingsScreen(
                authViewModel = authViewModel,
                budgetViewModel = budgetViewModel,
                movementsViewModel = koinViewModel(),
                onNavigateToBudgetLimits = {
                    navController.navigate(AppRoutes.BUDGET_LIMITS)
                },
                onEditBudget = { categoryName ->
                    // Find the category object by name and select it in the ViewModel
                    val budgetLimitsState = budgetViewModel.budgetLimitsScreenState.value
                    val category = budgetLimitsState.categoriesWithBudgets
                        .find { it.first.name == categoryName }?.first

                    if (category != null) {
                        budgetViewModel.onSelectCategory(category)
                        navController.navigate(AppRoutes.EDIT_BUDGET)
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
            SettingsScreen(
                authViewModel = authViewModel,
                settingsViewModel = koinViewModel(),
                onNavigateToBudgetLimits = {
                    navController.navigate(AppRoutes.BUDGET_LIMITS)
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.BUDGET_LIMITS) {
            val authState = authViewModel.authState.collectAsState()
            val userId = authState.value.user?.uid ?: ""

            androidx.compose.runtime.LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    budgetViewModel.fetchBudgetsAndCategories(userId)
                }
            }

            BudgetLimitsScreen(
                budgetViewModel = budgetViewModel,
                authViewModel = authViewModel,
                onSelectCategory = { category ->
                    budgetViewModel.onSelectCategory(category)
                    navController.navigate(AppRoutes.EDIT_BUDGET)
                },
                onBack = {
                    navController.popBackStack()
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.EDIT_BUDGET) {
            val editBudgetFormState = budgetViewModel.editBudgetFormState.collectAsState()
            val authState = authViewModel.authState.collectAsState()
            val userId = authState.value.user?.uid ?: ""

            androidx.compose.runtime.LaunchedEffect(Unit) {
                budgetViewModel.fetchStatuses()
            }

            EditBudgetForm(
                budgetFormState = editBudgetFormState.value,
                onAmountChange = budgetViewModel::onAmountChange,
                onSave = {
                    budgetViewModel.saveBudget(userId) {
                        navController.popBackStack()
                    }
                },
                onCancel = {
                    budgetViewModel.clearForm()
                    navController.popBackStack()
                },
                onDelete = if (editBudgetFormState.value.existingBudget != null) {
                    {
                        budgetViewModel.deleteBudget(
                            userId,
                            editBudgetFormState.value.existingBudget!!.id
                        ) {
                            navController.popBackStack()
                        }
                    }
                } else null,
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.SCAN_RECEIPT) {
            ScanReceiptScreen(
                scanReceiptViewModel = scanReceiptViewModel,
                onNavigateBack = {
                    scanReceiptViewModel.clearState()
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(AppRoutes.EDIT_SCANNED_RECEIPT)
                }
            )
        }
        composable(AppRoutes.EDIT_SCANNED_RECEIPT) {
            EditScannedReceiptScreen(
                scanReceiptViewModel = scanReceiptViewModel,
                newMovementFormViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    scanReceiptViewModel.clearState()
                    // Navigate back to home or movement list
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }
    }
}
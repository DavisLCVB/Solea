package com.grupo03.solea.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.grupo03.solea.presentation.viewmodels.screens.AudioAnalysisViewModel
import com.grupo03.solea.presentation.viewmodels.screens.BudgetViewModel
import com.grupo03.solea.presentation.viewmodels.screens.NewMovementFormViewModel
import com.grupo03.solea.presentation.viewmodels.screens.SavingsViewModel
import com.grupo03.solea.presentation.viewmodels.screens.ScanReceiptViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel
import com.grupo03.solea.ui.screens.forms.NewCategoryFormScreen
import com.grupo03.solea.ui.screens.forms.NewMovementFormScreen
import com.grupo03.solea.ui.screens.history.HistoryScreen
import com.grupo03.solea.ui.screens.home.HomeScreen
import com.grupo03.solea.ui.screens.savings.AddEditGoalScreen
import com.grupo03.solea.ui.screens.savings.GoalManagementScreen
import com.grupo03.solea.ui.screens.scanner.EditScannedReceiptScreen
import com.grupo03.solea.ui.screens.scanner.LoadingScanScreen
import com.grupo03.solea.ui.screens.scanner.ScanReceiptScreen
import com.grupo03.solea.ui.screens.voicenote.AudioAnalysisScreen
import com.grupo03.solea.ui.screens.voicenote.EditVoiceNoteScreen
import com.grupo03.solea.ui.screens.settings.BudgetLimitsScreen
import com.grupo03.solea.ui.screens.settings.CurrencySelectionScreen
import com.grupo03.solea.ui.screens.settings.EditBudgetForm
import com.grupo03.solea.ui.screens.settings.LanguageSelectionScreen
import com.grupo03.solea.ui.screens.settings.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.mainNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    budgetViewModel: BudgetViewModel,
    movementsViewModel: MovementsViewModel,
    scanReceiptViewModel: ScanReceiptViewModel,
    audioAnalysisViewModel: AudioAnalysisViewModel,
    contentPadding: PaddingValues,
) {
    navigation(
        startDestination = AppRoutes.HOME,
        route = AppRoutes.PREFIX
    ) {
        composable(AppRoutes.HOME) { backStackEntry ->
            val mainGraphEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.PREFIX) }
            val statisticsViewModel: com.grupo03.solea.presentation.viewmodels.screens.StatisticsViewModel = koinViewModel(viewModelStoreOwner = mainGraphEntry)

            HomeScreen(
                homeViewModel = koinViewModel(),
                movementsViewModel = koinViewModel(),
                authViewModel = authViewModel,
                statisticsViewModel = statisticsViewModel,
                onNavigateToNewMovement = {
                    navController.navigate(AppRoutes.NEW_MOVEMENT)
                },
                onNavigateToNewCategory = {
                    navController.navigate(AppRoutes.NEW_CATEGORY)
                },
                onNavigateToScanReceipt = {
                    navController.navigate(AppRoutes.SCAN_RECEIPT)
                },
                onNavigateToAudioAnalysis = {
                    navController.navigate(AppRoutes.AUDIO_ANALYSIS)
                },
                onNavigateToStatistics = {
                    navController.navigate(AppRoutes.STATISTICS)
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
        composable(AppRoutes.SAVINGS) { backStackEntry ->
            val mainGraphEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.PREFIX) }
            val savingsViewModel: SavingsViewModel = koinViewModel(viewModelStoreOwner = mainGraphEntry)

            com.grupo03.solea.ui.screens.savings.SavingsScreen(
                authViewModel = authViewModel,
                budgetViewModel = budgetViewModel,
                savingsViewModel = savingsViewModel,
                movementsViewModel = koinViewModel(),
                onNavigateToBudgetLimits = {
                    navController.navigate(AppRoutes.BUDGET_LIMITS)
                },
                onNavigateToGoalManagement = {
                    navController.navigate(AppRoutes.GOAL_MANAGEMENT)
                },
                onEditBudget = { categoryName ->
                    val budgetLimitsState = budgetViewModel.budgetLimitsScreenState.value
                    val category = budgetLimitsState.categoriesWithBudgets
                        .find { it.first.name == categoryName }?.first

                    if (category != null) {
                        budgetViewModel.onSelectCategory(category)
                        navController.navigate(AppRoutes.EDIT_BUDGET)
                    }
                },
                onNavigateToEditGoal = { goal ->
                    savingsViewModel.prepareFormForEdit(goal)
                    navController.navigate(AppRoutes.ADD_EDIT_GOAL)
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.SHOPPING_LIST) { backStackEntry ->
            val newMovementViewModel: NewMovementFormViewModel = koinViewModel()
            
            com.grupo03.solea.ui.screens.shoppinglist.ShoppingListScreen(
                onNavigateToEditList = {
                    navController.navigate(AppRoutes.EDIT_SHOPPING_LIST)
                },
                onNavigateToHistory = {
                    navController.navigate(AppRoutes.SHOPPING_LIST_HISTORY)
                },
                onNavigateToNewMovement = { item, _ ->
                    if (item != null) {
                        newMovementViewModel.prefillFromShoppingItem(
                            itemId = item.id,
                            itemName = item.name,
                            estimatedPrice = item.estimatedPrice
                        )
                        navController.navigate(AppRoutes.NEW_MOVEMENT)
                    }
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.EDIT_SHOPPING_LIST) {
            com.grupo03.solea.ui.screens.shoppinglist.EditShoppingListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppRoutes.SHOPPING_LIST_HISTORY) {
            com.grupo03.solea.ui.screens.shoppinglist.ShoppingListHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToViewList = { listId ->
                    navController.navigate("view_shopping_list/$listId")
                }
            )
        }
        composable(
            route = AppRoutes.VIEW_SHOPPING_LIST,
            arguments = listOf(
                androidx.navigation.navArgument("listId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            com.grupo03.solea.ui.screens.shoppinglist.ViewShoppingListScreen(
                listId = listId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                authViewModel = authViewModel,
                settingsViewModel = koinViewModel(),
                onNavigateToBudgetLimits = {
                    navController.navigate(AppRoutes.BUDGET_LIMITS)
                },
                onNavigateToCurrencySelection = {
                    navController.navigate(AppRoutes.CURRENCY_SELECTION)
                },
                onNavigateToLanguageSelection = {
                    navController.navigate(AppRoutes.LANGUAGE_SELECTION)
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.CURRENCY_SELECTION) {
            CurrencySelectionScreen(
                settingsViewModel = koinViewModel(),
                onNavigateBack = {
                    navController.popBackStack()
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.LANGUAGE_SELECTION) {
            LanguageSelectionScreen(
                settingsViewModel = koinViewModel(),
                onNavigateBack = {
                    navController.popBackStack()
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.STATISTICS) { backStackEntry ->
            val mainGraphEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.PREFIX) }
            val statisticsViewModel: com.grupo03.solea.presentation.viewmodels.screens.StatisticsViewModel = koinViewModel(viewModelStoreOwner = mainGraphEntry)

            com.grupo03.solea.ui.screens.statistics.StatisticsScreen(
                statisticsViewModel = statisticsViewModel,
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.BUDGET_LIMITS) {
            val authState by authViewModel.authState.collectAsState()
            val userId = authState.user?.uid ?: ""

            LaunchedEffect(userId) {
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
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(AppRoutes.EDIT_BUDGET) {
            val editBudgetFormState by budgetViewModel.editBudgetFormState.collectAsState()
            val authState by authViewModel.authState.collectAsState()
            val userId = authState.user?.uid ?: ""

            LaunchedEffect(Unit) {
                budgetViewModel.fetchStatuses()
            }

            EditBudgetForm(
                budgetFormState = editBudgetFormState,
                onAmountChange = budgetViewModel::onAmountChange,
                onSave = {
                    budgetViewModel.saveBudget(userId) { navController.popBackStack() }
                },
                onCancel = {
                    budgetViewModel.clearForm()
                    navController.popBackStack()
                },
                onDelete = if (editBudgetFormState.existingBudget != null) {
                    {
                        budgetViewModel.deleteBudget(userId, editBudgetFormState.existingBudget!!.id) { navController.popBackStack() }
                    }
                } else null,
                modifier = Modifier.padding(contentPadding)
            )
        }

        composable(AppRoutes.GOAL_MANAGEMENT) { backStackEntry ->
            val mainGraphEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.PREFIX) }
            val savingsViewModel: SavingsViewModel = koinViewModel(viewModelStoreOwner = mainGraphEntry)

            GoalManagementScreen(
                savingsViewModel = savingsViewModel,
                onNavigateToNewGoal = {
                    savingsViewModel.prepareFormForCreate()
                    navController.navigate(AppRoutes.ADD_EDIT_GOAL)
                },
                onNavigateToEditGoal = { goal ->
                    savingsViewModel.prepareFormForEdit(goal)
                    navController.navigate(AppRoutes.ADD_EDIT_GOAL)
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(contentPadding)
            )
        }

        composable(AppRoutes.ADD_EDIT_GOAL) { backStackEntry ->
            val mainGraphEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.PREFIX) }
            val savingsViewModel: SavingsViewModel = koinViewModel(viewModelStoreOwner = mainGraphEntry)

            AddEditGoalScreen(
                savingsViewModel = savingsViewModel,
                authViewModel = authViewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(contentPadding)
            )
        }

        composable(AppRoutes.SCAN_RECEIPT) {
            ScanReceiptScreen(
                scanReceiptViewModel = scanReceiptViewModel,
                authViewModel = authViewModel,
                onNavigateBack = {
                    scanReceiptViewModel.clearState()
                    navController.popBackStack()
                },
                onNavigateToLoading = {
                    navController.navigate(AppRoutes.LOADING_SCAN)
                }
            )
        }
        composable(AppRoutes.LOADING_SCAN) {
            LoadingScanScreen(
                scanReceiptViewModel = scanReceiptViewModel,
                onNavigateToEdit = {
                    navController.navigate(AppRoutes.EDIT_SCANNED_RECEIPT) {
                        popUpTo(AppRoutes.SCAN_RECEIPT) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    scanReceiptViewModel.clearState()
                    navController.popBackStack()
                }
            )
        }
        composable(AppRoutes.EDIT_SCANNED_RECEIPT) {
            EditScannedReceiptScreen(
                scanReceiptViewModel = scanReceiptViewModel,
                newMovementFormViewModel = koinViewModel(),
                newCategoryFormViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateBack = {
                    scanReceiptViewModel.clearState()
                    navController.popBackStack()
                },
                onSuccess = {
                    scanReceiptViewModel.clearState()
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }
        composable(AppRoutes.AUDIO_ANALYSIS) {
            AudioAnalysisScreen(
                audioAnalysisViewModel = audioAnalysisViewModel,
                authViewModel = authViewModel,
                onNavigateBack = {
                    audioAnalysisViewModel.clearState()
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(AppRoutes.EDIT_VOICE_NOTE)
                }
            )
        }
        composable(AppRoutes.EDIT_VOICE_NOTE) {
            EditVoiceNoteScreen(
                audioAnalysisViewModel = audioAnalysisViewModel,
                newMovementFormViewModel = koinViewModel(),
                newCategoryFormViewModel = koinViewModel(),
                authViewModel = authViewModel,
                onNavigateBack = {
                    audioAnalysisViewModel.clearState()
                    navController.popBackStack()
                },
                onSuccess = {
                    audioAnalysisViewModel.clearState()
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }
    }
}
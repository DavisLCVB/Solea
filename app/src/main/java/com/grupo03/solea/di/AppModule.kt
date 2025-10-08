package com.grupo03.solea.di

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.grupo03.solea.data.repositories.firebase.FirebaseBudgetRepository
import com.grupo03.solea.data.repositories.firebase.FirebaseCategoryRepository
import com.grupo03.solea.data.repositories.firebase.FirebaseItemRepository
import com.grupo03.solea.data.repositories.firebase.FirebaseMovementRepository
import com.grupo03.solea.data.repositories.firebase.FirebaseReceiptRepository
import com.grupo03.solea.data.repositories.interfaces.BudgetRepository
import com.grupo03.solea.data.repositories.interfaces.CategoryRepository
import com.grupo03.solea.data.repositories.interfaces.ItemRepository
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.data.repositories.interfaces.ReceiptRepository
import com.grupo03.solea.data.repositories.interfaces.UserPreferencesRepository
import com.grupo03.solea.data.repositories.local.DataStoreUserPreferencesRepository
import com.grupo03.solea.data.services.api.RetrofitReceiptScannerService
import com.grupo03.solea.data.services.firebase.FirebaseAuthService
import com.grupo03.solea.data.services.interfaces.AuthService
import com.grupo03.solea.data.services.interfaces.ReceiptScannerService
import com.grupo03.solea.presentation.viewmodels.screens.BudgetViewModel
import com.grupo03.solea.presentation.viewmodels.screens.HistoryViewModel
import com.grupo03.solea.presentation.viewmodels.screens.HomeViewModel
import com.grupo03.solea.presentation.viewmodels.screens.NewCategoryFormViewModel
import com.grupo03.solea.presentation.viewmodels.screens.NewMovementFormViewModel
import com.grupo03.solea.presentation.viewmodels.screens.ScanReceiptViewModel
import com.grupo03.solea.presentation.viewmodels.screens.SettingsViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module for the Solea application.
 *
 * This module defines all application dependencies and their scopes:
 * - **Firebase instances**: Singleton instances of Firebase Auth and Firestore
 * - **Services**: Authentication and receipt scanning services
 * - **Repositories**: Data access layer implementations
 * - **ViewModels**: Presentation layer view models
 *
 * All dependencies are resolved automatically by Koin based on constructor parameters.
 *
 * @see org.koin.core.module.Module
 */
val appModule = module {
    // ==================== Firebase instances ====================
    // Singleton Firebase Authentication instance
    single { Firebase.auth }

    // Singleton Firebase Firestore instance
    single { Firebase.firestore }

    // ==================== Services ====================
    // Authentication service using Firebase
    single<AuthService> { FirebaseAuthService(get()) }

    // Receipt scanning service using Retrofit API
    single<ReceiptScannerService> { RetrofitReceiptScannerService() }

    // ==================== Repositories ====================
    // Movement repository for financial transactions
    single<MovementRepository> { FirebaseMovementRepository(get()) }

    // Budget repository for spending limits
    single<BudgetRepository> { FirebaseBudgetRepository(get()) }

    // Category repository for expense classification
    single<CategoryRepository> { FirebaseCategoryRepository(get()) }

    // Item repository for purchase items
    single<ItemRepository> { FirebaseItemRepository(get()) }

    // Receipt repository for scanned receipts
    single<ReceiptRepository> { FirebaseReceiptRepository(get()) }

    // User preferences repository using DataStore
    single<UserPreferencesRepository> { DataStoreUserPreferencesRepository(androidContext()) }

    // ==================== ViewModels ====================
    // Shared view model for authentication state
    viewModel { AuthViewModel(get()) }

    // Home screen view model
    viewModel { HomeViewModel() }

    // Shared view model for movements data
    viewModel { MovementsViewModel(get(), get()) }

    // View model for creating new categories
    viewModel { NewCategoryFormViewModel(get()) }

    // View model for creating new movements
    viewModel { NewMovementFormViewModel(get(), get(), get(), get()) }

    // View model for budget management
    viewModel { BudgetViewModel(get(), get()) }

    // View model for application settings
    viewModel { SettingsViewModel(get()) }

    // View model for transaction history
    viewModel { HistoryViewModel(get()) }

    // View model for receipt scanning feature
    viewModel { ScanReceiptViewModel(get(), get()) }
}

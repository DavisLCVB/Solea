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

val appModule = module {
    // Firebase instances
    single { Firebase.auth }
    single { Firebase.firestore }

    // Services
    single<AuthService> { FirebaseAuthService(get()) }
    single<ReceiptScannerService> { RetrofitReceiptScannerService() }

    // Repositories
    single<MovementRepository> { FirebaseMovementRepository(get()) }
    single<BudgetRepository> { FirebaseBudgetRepository(get()) }
    single<CategoryRepository> { FirebaseCategoryRepository(get()) }
    single<ItemRepository> { FirebaseItemRepository(get()) }
    single<ReceiptRepository> { FirebaseReceiptRepository(get()) }
    single<UserPreferencesRepository> { DataStoreUserPreferencesRepository(androidContext()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel() }
    viewModel { MovementsViewModel(get(), get()) }
    viewModel { NewCategoryFormViewModel(get()) }
    viewModel { NewMovementFormViewModel(get(), get(), get(), get()) }
    viewModel { BudgetViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { ScanReceiptViewModel(get(), get()) }
}

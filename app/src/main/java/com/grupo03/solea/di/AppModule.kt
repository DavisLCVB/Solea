package com.grupo03.solea.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo03.solea.data.repositories.firebase.FirebaseBudgetRepository
import com.grupo03.solea.data.repositories.firebase.FirebaseMovementRepository
import com.grupo03.solea.data.repositories.firebase.FirebaseSavingsGoalRepository
import com.grupo03.solea.data.repositories.firebase.FirebaseUserRepository
import com.grupo03.solea.data.repositories.interfaces.AuthRepository
import com.grupo03.solea.data.repositories.interfaces.BudgetRepository
import com.grupo03.solea.data.repositories.interfaces.MovementRepository
import com.grupo03.solea.data.repositories.interfaces.SavingsGoalRepository
import com.grupo03.solea.data.repositories.interfaces.UserRepository
import com.grupo03.solea.presentation.viewmodels.screens.HistoryViewModel
import com.grupo03.solea.presentation.viewmodels.screens.HomeViewModel
import com.grupo03.solea.presentation.viewmodels.screens.LoginViewModel
import com.grupo03.solea.presentation.viewmodels.screens.RegisterViewModel
import com.grupo03.solea.presentation.viewmodels.screens.SavingsViewModel
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.presentation.viewmodels.shared.BudgetViewModel
import com.grupo03.solea.presentation.viewmodels.shared.MovementsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
}

val appModule = module {
    single<AuthRepository> { FirebaseUserRepository(get()) }
    single<UserRepository> { FirebaseUserRepository(get()) }
    single<MovementRepository> { FirebaseMovementRepository(get()) }
    single<BudgetRepository> { FirebaseBudgetRepository(get()) }
    single<SavingsGoalRepository> { FirebaseSavingsGoalRepository(get()) }
}

val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { MovementsViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { BudgetViewModel(get()) }
    viewModel { SavingsViewModel(get(), get()) }
}
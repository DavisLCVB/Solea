package com.grupo03.solea.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel
import com.grupo03.solea.ui.screens.auth.SignInScreen
import com.grupo03.solea.ui.screens.auth.SignUpScreen
import com.grupo03.solea.ui.screens.auth.WelcomeScreen

fun NavGraphBuilder.authNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = AuthRoutes.WELCOME,
        route = AuthRoutes.PREFIX
    ) {
        composable(
            route = AuthRoutes.LOGIN,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            }
        ) {
            SignInScreen(
                viewModel = authViewModel,
                navigateToSignUp = {
                    navController.navigate(AuthRoutes.SIGN_UP)
                },
            )
        }
        composable(
            route = AuthRoutes.SIGN_UP,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            }) {
            SignUpScreen(
                viewModel = authViewModel,
                navigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.LOGIN) {
                            inclusive = true
                        }
                    }
                },
            )

        }
        composable(route = AuthRoutes.WELCOME) {
            WelcomeScreen(
                navigateToSignIn = {
                    navController.navigate(AuthRoutes.LOGIN)
                },
                navigateToSignUp = {
                    navController.navigate(AuthRoutes.SIGN_UP)
                },
            )
        }

    }
}
package com.grupo03.solea.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.grupo03.solea.R
import com.grupo03.solea.ui.navigation.AppRoutes

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val title: Int
) {
    object Home : BottomNavItem(
        AppRoutes.HOME, R.drawable.icons_home,
        R.string.nav_home_title
    )

    object History : BottomNavItem(
        AppRoutes.HISTORY, R.drawable.icons_history,
        R.string.nav_history_title
    )

    object Savings : BottomNavItem(
        AppRoutes.SAVINGS, R.drawable.icons_savings,
        R.string.nav_savings_title
    )

    object ShoppingList :
        BottomNavItem(
            AppRoutes.SHOPPING_LIST, R.drawable.icons_shopping_list,
            R.string.nav_shopping_list_title
        )

    object Settings : BottomNavItem(
        AppRoutes.SETTINGS, R.drawable.icons_settings,
        R.string.nav_settings_title

    )
}


@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val items = listOf(
        BottomNavItem.History,
        BottomNavItem.Home,
        BottomNavItem.Savings,
        BottomNavItem.Settings,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.title)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.title),
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
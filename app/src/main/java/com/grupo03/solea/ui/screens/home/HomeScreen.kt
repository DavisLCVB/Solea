package com.grupo03.solea.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.presentation.viewmodels.AuthViewModel

import com.grupo03.solea.ui.navigation.mainNavigationGraph

// Data classes para los datos mock
data class FinancialData(
    val currentBalance: Double = 1250.00,
    val income: Double = 2000.00,
    val expenses: Double = 750.00,
    val snacksLimit: Double = 40.00,
    val snacksSpent: Double = 22.00, // 55% of limit
    val laptopGoal: Double = 1600.00,
    val laptopSaved: Double = 160.00 // 10% of goal
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
) {
    val navController = androidx.navigation.compose.rememberNavController()
    var selectedBottomItem by remember { mutableIntStateOf(2) } // "Inicio" selected (index 2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedBottomItem) {
                            0 -> "Perfil"
                            1 -> "Ahorro"
                            2 -> "Solea"
                            3 -> "Historial"
                            4 -> "Lista de compras"
                            else -> "Solea"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedBottomItem,
                onItemSelected = {
                    selectedBottomItem = it
                    when (it) {
                        0 -> {/* Perfil, no navega */}
                        1 -> navController.navigate(com.grupo03.solea.ui.navigation.AppRoutes.SAVINGS)
                        2 -> navController.navigate(com.grupo03.solea.ui.navigation.AppRoutes.HOME)
                        3 -> navController.navigate(com.grupo03.solea.ui.navigation.AppRoutes.HISTORY)
                        4 -> navController.navigate(com.grupo03.solea.ui.navigation.AppRoutes.SHOPPING_LIST)
                    }
                }
            )
        }
    ) { paddingValues ->
        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = com.grupo03.solea.ui.navigation.AppRoutes.HOME
        ) {
            mainNavigationGraph(authViewModel = authViewModel, navController = navController, contentPadding = paddingValues)
        }
    }
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    financialData: FinancialData = FinancialData()
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Card
        BalanceCard(financialData = financialData)
        
        // Income vs Expenses Chart Placeholder
        ChartCard()
        
        // Alerts/Notifications
        AlertsSection(financialData = financialData)
        
        // Add some bottom spacing
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun BalanceCard(financialData: FinancialData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Saldo actual",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "S/ ${String.format("%.2f", financialData.currentBalance)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ingresos: S/ ${String.format("%.2f", financialData.income)}",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50), // Green for income
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Gastos: S/ ${String.format("%.2f", financialData.expenses)}",
                    fontSize = 12.sp,
                    color = Color(0xFFF44336), // Red for expenses
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Ingresos vs gastos",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Placeholder for chart - you can replace this with actual chart later
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Gráfico de ingresos vs gastos\n(Por implementar)",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AlertsSection(financialData: FinancialData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Snacks spending alert
        val snacksPercentage = (financialData.snacksSpent / financialData.snacksLimit * 100).toInt()
        AlertCard(
            icon = Icons.Default.Warning,
            iconColor = MaterialTheme.colorScheme.secondary,
            title = "Superaste el 80% del límite en snacks",
            amount = "S/${String.format("%.2f", financialData.snacksSpent)}",
            percentage = "+$snacksPercentage%",
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        // Laptop savings progress
        val laptopPercentage = (financialData.laptopSaved / financialData.laptopGoal * 100).toInt()
        AlertCard(
            icon = Icons.Default.CheckCircle,
            iconColor = MaterialTheme.colorScheme.tertiary,
            title = "Llevas ahorrado el 50% de tu meta de laptop",
            amount = "S/${String.format("%.2f", financialData.laptopSaved)}",
            percentage = "-$laptopPercentage%",
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun AlertCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    amount: String,
    percentage: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Warning/Success Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = contentColor,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Amount and percentage
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = amount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = percentage,
                    fontSize = 12.sp,
                    color = if (percentage.startsWith("+")) MaterialTheme.colorScheme.error else iconColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        BottomNavItem("Perfil", Icons.Default.Person, hasNotification = true),
        BottomNavItem("Ahorro", Icons.Default.Star, hasNotification = false),
        BottomNavItem("Inicio", Icons.Default.Home, hasNotification = false),
        BottomNavItem("Historial", Icons.Default.Star, hasNotification = true),
        BottomNavItem("Lista", Icons.Default.List, hasNotification = false)
    )
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Box {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                        if (item.hasNotification) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFF44336), CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val hasNotification: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(
            modifier = Modifier.fillMaxSize()
        )
    }
}
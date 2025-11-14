package com.grupo03.solea.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Budget
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.utils.CurrencyUtils

@Composable
fun BudgetLimitsScreen(
    budgetViewModel: com.grupo03.solea.presentation.viewmodels.screens.BudgetViewModel,
    authViewModel: com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel,
    onSelectCategory: (Category) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budgetState = budgetViewModel.budgetLimitsScreenState.collectAsState()

    // Separar categorías con y sin límite
    val categoriesWithLimit = budgetState.value.categoriesWithBudgets.filter { it.second != null }
    val categoriesWithoutLimit =
        budgetState.value.categoriesWithBudgets.filter { it.second == null }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.budget_limits_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.budget_limits_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Categorías con límite
            if (categoriesWithLimit.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.with_limit_set),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(categoriesWithLimit) { (category, budget) ->
                    CategoryBudgetCard(
                        category = category,
                        budget = budget,
                        onClick = { onSelectCategory(category) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Categorías sin límite
            if (categoriesWithoutLimit.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.without_limit),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(categoriesWithoutLimit) { (category, budget) ->
                    CategoryBudgetCard(
                        category = category,
                        budget = budget,
                        onClick = { onSelectCategory(category) }
                    )
                }
            }
        }
    }
}

// Helper function to get icon for category
@Composable
fun getCategoryIcon(categoryName: String): ImageVector {
    val categoryFood = stringResource(R.string.category_food)
    val categoryRestaurant = stringResource(R.string.category_restaurant)
    val categoryTransportation = stringResource(R.string.category_transportation)
    val categoryTaxi = stringResource(R.string.category_taxi)
    val categoryUber = stringResource(R.string.category_uber)
    val categoryEntertainment = stringResource(R.string.category_entertainment)
    val categoryHealth = stringResource(R.string.category_health)
    val categoryMedicine = stringResource(R.string.category_medicine)
    val categoryHome = stringResource(R.string.category_home)
    val categoryShopping = stringResource(R.string.category_shopping)
    val categoryEducation = stringResource(R.string.category_education)
    val categoryServices = stringResource(R.string.category_services)
    val categoryClothing = stringResource(R.string.category_clothing)
    val categoryTechnology = stringResource(R.string.category_technology)

    return when (categoryName.lowercase()) {
        categoryFood, categoryRestaurant -> Icons.Default.Restaurant
        categoryTransportation, categoryTaxi, categoryUber -> Icons.Default.DirectionsCar
        categoryEntertainment -> Icons.Default.Movie
        categoryHealth, categoryMedicine -> Icons.Default.LocalHospital
        categoryHome -> Icons.Default.Home
        categoryShopping -> Icons.Default.ShoppingCart
        categoryEducation -> Icons.Default.School
        categoryServices -> Icons.Default.Build
        categoryClothing -> Icons.Default.Checkroom
        categoryTechnology -> Icons.Default.Computer
        else -> Icons.Default.Category
    }
}

@Composable
fun CategoryBudgetCard(
    category: Category,
    budget: Budget?,
    onClick: () -> Unit
) {
    val icon = getCategoryIcon(category.name)
    val hasLimit = budget != null
    val currencySymbol = CurrencyUtils.getDeviceCurrencySymbol()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .run {
                        background(
                            if (hasLimit)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (hasLimit)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Category info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (budget != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.limit_amount_format, currencySymbol, budget.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status indicator
            if (budget != null) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            stringResource(R.string.active_status),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.primary,
                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.configure_button),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

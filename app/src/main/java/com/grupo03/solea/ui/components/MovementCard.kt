package com.grupo03.solea.ui.components

import androidx.compose.foundation.border
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.Expense
import com.grupo03.solea.data.models.ExpenseDetails
import com.grupo03.solea.data.models.Income
import com.grupo03.solea.data.models.IncomeDetails
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.ui.theme.soleaGreen
import com.grupo03.solea.ui.theme.soleaRed
import java.time.LocalDateTime

@Composable
fun MovementCard(
    modifier: Modifier = Modifier,
    category: Category = Category(),
    incomeDetails: IncomeDetails? = null,
    expenseDetails: ExpenseDetails? = null,
) {
    if (incomeDetails == null && expenseDetails == null) return
    if (incomeDetails != null) {
        IncomeCard(
            modifier = modifier,
            incomeDetails = incomeDetails,
            category = category,
        )
    } else if (expenseDetails != null) {
        ExpenseCard(
            modifier = modifier,
            expenseDetails = expenseDetails,
            category = category,
        )
    }
}

@Composable
fun IncomeCard(
    modifier: Modifier = Modifier,
    incomeDetails: IncomeDetails = IncomeDetails(),
    category: Category = Category(),
) {
    val movement = incomeDetails.movement
    val dateFormated = "${movement.datetime.toLocalDate()} ${
        movement.datetime.toLocalTime().toString().substring(0, 5)
    }"
    val name = movement.name.ifEmpty { movement.description.ifEmpty { "Sin nombre" } }
    val amountText = "${movement.currency} ${String.format(Locale.getDefault(), "%.2f", movement.total)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 2.dp,
                        color = soleaGreen,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_up),
                    contentDescription = "Arrow Up",
                    tint = soleaGreen,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Name and Date (no category for income)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormated,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = soleaGreen
            )
        }
    }

}

@Composable
fun ExpenseCard(
    modifier: Modifier = Modifier,
    expenseDetails: ExpenseDetails = ExpenseDetails(),
    category: Category = Category(),
) {
    val movement = expenseDetails.movement
    val dateFormated = "${movement.datetime.toLocalDate()} ${
        movement.datetime.toLocalTime().toString().substring(0, 5)
    }"
    val name = movement.name.ifEmpty { movement.description.ifEmpty { "Sin nombre" } }
    val amountText = "${movement.currency} ${String.format(Locale.getDefault(), "%.2f", movement.total)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 2.dp,
                        color = soleaRed,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_down),
                    contentDescription = "Arrow Down",
                    tint = soleaRed,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Name, Category and Date
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = movement.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormated,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = soleaRed
            )
        }
    }
}


val incomeCategoryMockup = Category(
    id = "1",
    name = "Incomes",
    description = "Income for the user",
)

val incomeDetailsMockup = IncomeDetails(
    income = Income(
        id = "2",
    ),
    movement = Movement(
        id = "3",
        type = MovementType.INCOME,
        name = "Salario mensual",
        description = "Pago del mes de enero",
        datetime = LocalDateTime.now(),
        currency = "USD",
        total = 2500.0,
        category = incomeCategoryMockup.name,
        createdAt = LocalDateTime.now()
    )
)

val expenseCategoryMockup = Category(
    id = "4",
    name = "Food",
    description = "Food for the user",
)

val expenseDetailsMockup = ExpenseDetails(
    expense = Expense(
        id = "5",
    ),
    movement = Movement(
        id = "6",
        type = MovementType.EXPENSE,
        name = "Almuerzo",
        description = "Comida en restaurante",
        datetime = LocalDateTime.now(),
        currency = "USD",
        total = 45.50,
        category = expenseCategoryMockup.name,
        createdAt = LocalDateTime.now()
    )
)

@Preview(showBackground = true)
@Composable
fun MovementCardPreviewIncome() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface {
            MovementCard(
                incomeDetails = incomeDetailsMockup,
                expenseDetails = null,
                category = incomeCategoryMockup
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovementCardPreviewExpense() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface(
        ) {
            MovementCard(
                incomeDetails = null,
                expenseDetails = expenseDetailsMockup,
                category = expenseCategoryMockup
            )
        }
    }
}
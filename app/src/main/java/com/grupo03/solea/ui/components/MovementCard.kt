package com.grupo03.solea.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo03.solea.R
import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.ui.theme.SoleaTheme
import com.grupo03.solea.ui.theme.soleaGreen
import com.grupo03.solea.ui.theme.soleaRed
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MovementCard(
    modifier: Modifier = Modifier,
    movement: Movement = Movement(),
    movementType: MovementType = MovementType()
) {
    val zone = ZoneId.systemDefault()
    val format = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(zone)
    val dateFormated = format.format(movement.date)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(
                    width = 2.dp,
                    color = if (movement.amount >= 0) soleaGreen else soleaRed,
                    shape = RoundedCornerShape(50)
                )
                .weight(1f)
        ) {
            Icon(
                painter = painterResource(if (movement.amount >= 0) R.drawable.arrow_up else R.drawable.arrow_down),
                contentDescription = "Arrow Down",
                tint = if (movement.amount >= 0) soleaGreen else soleaRed,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(10.dp)
            )
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(4f)
        ) {
            Text(
                text = movement.item,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${movementType.value}, at $dateFormated",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = if (movement.amount >= 0) "+$${"%.2f".format(movement.amount)}" else "-$${
                "%.2f".format(
                    -movement.amount
                )
            }",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (movement.amount >= 0) soleaGreen else soleaRed,
            modifier = Modifier
                .weight(2f)
                .padding(start = 16.dp, end = 8.dp)
                .align(Alignment.CenterVertically),
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MovementCardPreviewNegative() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface {
            MovementCard(
                movement = Movement(
                    amount = -10.0, item = "Oreo Cookies",
                ),
                movementType = MovementType(
                    value = "Snacks",
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovementCardPreviewPositive() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface(
        ) {
            MovementCard(
                movement = Movement(amount = 10.0, item = "Salary"),
                movementType = MovementType(value = "Income"),
            )
        }
    }
}
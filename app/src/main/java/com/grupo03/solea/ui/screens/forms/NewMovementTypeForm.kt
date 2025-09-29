package com.grupo03.solea.ui.screens.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo03.solea.R
import com.grupo03.solea.ui.theme.SoleaTheme

@Composable
fun NewMovementTypeForm(
    modifier: Modifier = Modifier,
    movementTypeName: String = "",
    onNameChange: (String) -> Unit = {},
    movementTypeDescription: String = "",
    onDescriptionChange: (String) -> Unit = {},
    onCreateType: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.weight(3f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add a new type of movement",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = movementTypeName,
                onValueChange = onNameChange,
                label = { Text("Type Name") },
                modifier = Modifier.padding(top = 20.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = movementTypeDescription,
                onValueChange = onDescriptionChange,
                label = { Text("Type Description") },
                modifier = Modifier.padding(top = 20.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(R.drawable.info),
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "This type will be available when adding new movements.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Button(
                onClick = onCreateType,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create Type",
                )
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                )
            }

        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun NewMovementTypeFormPreview() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface {
            NewMovementTypeForm(
                movementTypeName = "Snacks",
                movementTypeDescription = "Food and drinks outside of main meals",
            )
        }
    }
}
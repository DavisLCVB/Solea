package com.grupo03.solea.ui.screens.forms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo03.solea.ui.theme.SoleaTheme

@Composable
fun NewMovementForm(
    modifier: Modifier = Modifier,
    movementAmount: String = "",
    onMovementAmountChange: (String) -> Unit = {},
    typeList: List<String> = emptyList(),
    onTypeSelected: (String) -> Unit = {},
    typeSelected: String = "",
    note: String = "",
    onNoteChange: (String) -> Unit = {},
    onCreateMovement: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    var expanded by remember {
        mutableStateOf(false)
    }
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
                text = "Add a new movement",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = movementAmount,
                onValueChange = onMovementAmountChange,
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.padding(top = 20.dp)
            )
            Row {
                Text(
                    text = "Type: ",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(top = 20.dp, end = 10.dp)
                        .weight(1f)
                )
                Box(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .weight(1.5f)
                ) {
                    Button(
                        onClick = { expanded = true },
                    ) {
                        Row {
                            Text(
                                text = typeSelected,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        typeList.forEach { text ->
                            DropdownMenuItem(
                                text = { Text(text) },
                                onClick = {
                                    onTypeSelected(text)
                                    expanded = false
                                }
                            )
                        }
                    }

                }

            }
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Note (optional)") },
                modifier = Modifier.padding(top = 20.dp)
            )
            Button(
                onClick = onCreateMovement,
                modifier = Modifier
                    .padding(top = 30.dp, bottom = 10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create Movement",
                )
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
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
fun NewMovementFormPreview() {
    SoleaTheme(
        darkTheme = true
    ) {
        Surface {
            NewMovementForm(
                movementAmount = "150.0",
                typeList = listOf("Food", "Transport", "Entertainment"),
                typeSelected = "Food",
                note = "Lunch with friends"
            )
        }
    }
}
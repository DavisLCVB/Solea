package com.grupo03.solea.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grupo03.solea.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementModalBottomSheet(
    onDismissRequest: () -> Unit = {},
    onAddMovement: () -> Unit = {},
    onAddCategory: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
        ) {
            Text(text = stringResource(R.string.button_add_movement))
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(3f)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismissRequest()
                            onAddMovement()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row {
                            Box(
                                contentAlignment = Alignment.TopStart
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.form),
                                    contentDescription = stringResource(R.string.button_add_movement_by_form),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = stringResource(R.string.button_add_movement_by_form))
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            onDismissRequest()
                            onAddCategory()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                contentAlignment = Alignment.TopStart,

                                ) {
                                Icon(
                                    painter = painterResource(R.drawable.form),
                                    contentDescription = stringResource(R.string.button_add_category_by_form)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(text = stringResource(R.string.button_add_category_by_form))
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
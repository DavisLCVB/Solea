package com.grupo03.solea.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.grupo03.solea.presentation.viewmodels.AuthViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
) {
    HomeScreenContent(
        onSignOut = authViewModel::signOut,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = { }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Home Screen",
        )
        Button(
            onClick = onSignOut
        ) {
            Text(text = "Sign Out")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        modifier = Modifier.fillMaxSize()
    )
}
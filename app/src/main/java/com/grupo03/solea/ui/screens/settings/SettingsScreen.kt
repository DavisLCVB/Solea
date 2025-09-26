package com.grupo03.solea.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo03.solea.presentation.viewmodels.AuthViewModel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var themeIsLight by remember { mutableStateOf(true) }
    val userName = "Octavio Suárez" // mock
    val userEmail = "octavio.suarez@gmail.com" // mock

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Perfil / Configuración",
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        // User Info
        Text(
            text = "Información de Usuario",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Nombre", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(userName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Correo", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(userEmail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /* TODO: Editar perfil */ },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Editar Perfil")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        // Gastos
        Text("Configuración de Gastos", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notificaciones de Gastos", modifier = Modifier.weight(1f))
            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp).padding(vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Text(
                    "Establecer Límites por Categoría",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 15.sp
                )
            }
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp).padding(vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Exportar datos",
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        fontSize = 15.sp
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Exportar")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        // General
        Text("General", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Idioma de la aplicación", modifier = Modifier.weight(1f))
            Button(
                onClick = { /* TODO: Cambiar idioma */ },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Español", fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tema de Color", modifier = Modifier.weight(1f))
            Switch(checked = themeIsLight, onCheckedChange = { themeIsLight = it })
            Text(if (themeIsLight) "Claro" else "Oscuro", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Sobre la aplicación", fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Text("Versión 1.0.1", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(16.dp))
        // Cerrar sesión
        Button(
            onClick = { authViewModel.signOut() },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

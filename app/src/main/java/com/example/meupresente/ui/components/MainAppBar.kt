package com.example.meupresente.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
//import androidx.compose.material.icons.filled.Logout
//import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp // Import para usar unidades de texto
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.meupresente.R // Certifique-se de que o R seja importado corretamente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
    //title: String,
    userName: String, // Novo parâmetro para o nome do usuário
    screenTitle: String, // Novo parâmetro para o título da tela
    navController: NavController
) {
    TopAppBar(
        //title = { Text(text = title) },
        title = {
            Column {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
        actions = {
            // Botão de Logout
            IconButton(onClick = {
                // Lógica para deslogar e voltar para a tela de login
                navController.navigate("login") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }) {
        Icon(
            Icons.AutoMirrored.Filled.ExitToApp, "Logout"
        )
    }
}
)
}
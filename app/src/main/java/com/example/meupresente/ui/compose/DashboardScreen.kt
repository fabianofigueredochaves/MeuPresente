package com.example.meupresente.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
//import androidx.compose.material.icons.outlined.CardGiftcard
//import androidx.compose.material.icons.outlined.People
//import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meupresente.ui.components.MainAppBar // Vamos criar este no próximo passo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userId: Long,
    navController: NavController
) {
    Scaffold(
        topBar = {
            // Usando nosso AppBar reutilizável
            MainAppBar(title = "User", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "O que você gostaria de fazer?",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Botão para a Lista de Desejos
            DashboardButton(
                text = "Minha Lista de Desejos",
            icon = Icons.Outlined.CheckCircle,
            onClick = {
                navController.navigate("home/$userId")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botão para Gerenciar Amigos
            DashboardButton(
                text = "Adicionar e Remover Amigos",
            icon = Icons.Outlined.Face,
            onClick = {
                // Criaremos esta rota no futuro
                // navController.navigate(\"manage_friends/$userId\")
            }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botão para Visualizar Amigos
            DashboardButton(
                text = "Visualizar Lista de Amigos",
            icon = Icons.Outlined.AccountBox,
            onClick = {
                // E esta também
                // navController.navigate(\"friends_list/$userId\")
            }
            )
        }
    }
}

/**
 * Um botão de painel estilizado e reutilizável.
 */
@Composable
private fun DashboardButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
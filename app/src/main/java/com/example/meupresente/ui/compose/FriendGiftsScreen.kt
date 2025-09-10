package com.example.meupresente.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext // Import adicionado
import com.example.meupresente.ViewModel.MeuPresenteApplication // Import adicionado
import androidx.compose.runtime.LaunchedEffect // Import adicionado
import androidx.compose.runtime.remember // Import adicionado
import androidx.compose.runtime.mutableStateOf // Import adicionado
import androidx.compose.runtime.getValue // Import adicionado
import androidx.compose.runtime.setValue // Import adicionado
import com.example.meupresente.ui.components.MainAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendGiftsScreen(
    userId: Long, // Adicionado userId para receber da navegação
    friendEmail: String,
    navController: NavController
) {
    val context = LocalContext.current
    val appRepository = (context.applicationContext as MeuPresenteApplication).repository
    var userName by remember { mutableStateOf("Carregando...") } // Estado padrão

    LaunchedEffect(userId) { // Busca o nome do usuário
        val user = appRepository.getUserById(userId)
        userName = user?.name ?: "Usuário Desconhecido"
    }

    Scaffold(
        topBar = {
            // Reutiliza o MainAppBar com um título dinâmico
         //   MainAppBar(title = "Presentes de ${friendEmail}", navController = navController)
            MainAppBar(userName = userName, screenTitle = "Presentes de ${friendEmail}", navController = navController) // Atualizado
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
                text = "Lista de Presentes de ${friendEmail}",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Esta funcionalidade será implementada em uma próxima etapa, " +
                        "utilizando a sincronização com o Firebase para carregar os presentes desejados " +
                        "e não desejados de seus amigos.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Por enquanto, a tabela de 'gifts' local é usada apenas para os seus próprios presentes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

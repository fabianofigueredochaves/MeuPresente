package com.example.meupresente.ui.compose

import androidx.compose.foundation.clickable // Import adicionado
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meupresente.data.AppRepository
// Importação de User não é mais estritamente necessária para a Factory, mas pode ser mantida se houver planos futuros.
// import com.example.meupresente.models.User
import com.example.meupresente.ui.components.MainAppBar
import com.example.meupresente.ViewModel.ManageFriendsEvent
import com.example.meupresente.ViewModel.ManageFriendsViewModel
import com.example.meupresente.ViewModel.MeuPresenteApplication
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder // Import adicionado para codificar o e-mail na navegação
import java.nio.charset.StandardCharsets // Import adicionado para especificar o charset na codificação

// Sua Factory continua a mesma, pois ainda precisa do AppRepository e userId.
class ManageFriendsViewModelFactory(private val repository: AppRepository, private val userId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageFriendsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageFriendsViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFriendsScreen(
    userId: Long,
    navController: NavController,
    repository: AppRepository
) {

    val context = LocalContext.current
    val appRepository = (context.applicationContext as MeuPresenteApplication).repository // Precisamos do repositório para buscar o nome do usuário
    var userName by remember { mutableStateOf("Carregando...") } // Estado padrão
    LaunchedEffect(userId) { // Dispara quando o userId muda
    val user = appRepository.getUserById(userId)
    userName = user?.name ?: "Usuário Desconhecido"
    }

    val factory = ManageFriendsViewModelFactory(repository, userId)
    val viewModel: ManageFriendsViewModel = viewModel(factory = factory)

    val friendEmailInput by viewModel.friendEmailInput.collectAsState()
    val friendsList by viewModel.friends.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Coleta eventos do ViewModel para mostrar mensagens (SnackBar)
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is ManageFriendsEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
      //  topBar = { MainAppBar(title = "Gerenciar Amigos", navController = navController) },
        topBar = { MainAppBar(userName = userName, screenTitle = "Gerenciar Amigos", navController = navController) }, // Atualizado
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- Seção para Adicionar Amigo ---
            Text(
                "Adicionar Amigo por E-mail",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = friendEmailInput,
                    onValueChange = viewModel::onFriendEmailInputChange,
                    label = { Text("E-mail do Amigo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(onClick = viewModel::addFriend) {
                    Text("Adicionar")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Lista de Amigos Atuais ---
            Text(
                "Meus Amigos",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (friendsList.isEmpty()) {
                Text(
                    "Você ainda não tem amigos adicionados.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(friendsList) { friendEmail ->
                        FriendItem(
                            friendEmail = friendEmail,
                            onRemoveFriend = { viewModel.removeFriend(friendEmail) },
                            onViewGifts = {
                                // Codifica o e-mail para que possa ser passado com segurança na URL da rota
                               // val encodedEmail = URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                               // navController.navigate("friend_gifts/$encodedEmail")
                                val encodedEmail = URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) // Atualizado para incluir userId
                                navController.navigate("friend_gifts/$userId/$encodedEmail") // Passa o userId também
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
// Tornei a função pública para facilitar a visibilidade, se FriendGiftsScreen for em outro arquivo/pacote.
// Em um projeto maior, você pode mover esta Composable para um arquivo de componentes separável.
fun FriendItem(
    friendEmail: String, // E-mail do amigo
    onRemoveFriend: () -> Unit, // Callback para remover o amigo
    onViewGifts: (String) -> Unit // Novo callback para visualizar os presentes
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Aplica o modificador clickable diretamente no Card para que toda a área do Card seja clicável
            .clickable { onViewGifts(friendEmail) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Para espaçar o texto e o botão
        ) {
            Text(
                text = friendEmail,
                modifier = Modifier.weight(1f), // Faz o texto ocupar o máximo de espaço possível
                style = MaterialTheme.typography.bodyLarge
            )

            // Não é necessário Spacer(modifier = Modifier.weight(1f)) se usar SpaceBetween no horizontalArrangement
            // Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onRemoveFriend,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remover")
            }
        }
    }
}
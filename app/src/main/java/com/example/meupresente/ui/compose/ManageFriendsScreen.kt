package com.example.meupresente.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.User // Ainda pode ser necessário para a Factory
import com.example.meupresente.ui.components.MainAppBar
import com.example.meupresente.ViewModel.ManageFriendsEvent
import com.example.meupresente.ViewModel.ManageFriendsViewModel
import kotlinx.coroutines.flow.collectLatest

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
    val factory = ManageFriendsViewModelFactory(repository, userId)
    val viewModel: ManageFriendsViewModel = viewModel(factory = factory)

    val friendEmailInput by viewModel.friendEmailInput.collectAsState()
    // 👇 MUDANÇA AQUI 👇: friendsList agora é List<String>
    val friendsList by viewModel.friends.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Coleta eventos do ViewModel para mostrar mensagens
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
        topBar = { MainAppBar(title = "Gerenciar Amigos", navController = navController) },
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
                    // �� MUDANÇA AQUI 👇: items agora aceita String
                    items(friendsList) { friendEmail ->
                        // 👇 MUDANÇA AQUI 👇: Chamada para FriendItem com apenas o email
                        FriendItem(
                            friendEmail = friendEmail,
                            onRemoveFriend = { viewModel.removeFriend(friendEmail) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
// 👇 MUDANÇA AQUI 👇: FriendItem agora recebe String
private fun FriendItem(
    friendEmail: String,
    onRemoveFriend: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 👇 MUDANÇA AQUI 👇: Exibindo apenas o email
            Text(text = friendEmail, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.weight(1f)) // Empurra o botão para a direita

            Button(onClick = onRemoveFriend, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Remover")
            }
        }
    }
}

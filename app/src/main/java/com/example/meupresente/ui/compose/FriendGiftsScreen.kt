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
import androidx.compose.runtime.collectAsState // Import para collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.MutableState
import com.example.meupresente.models.Gift
import com.example.meupresente.ui.components.MainAppBar
import kotlinx.coroutines.flow.flowOf

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

    // Estado para o nome do amigo e seus presentes
    var friendDisplayName by remember { mutableStateOf("Carregando...") }

    //val friendGifts by remember { mutableStateOf(emptyList<Gift>()) }.collectAsState() // CollectAsState precisa de um StateFlow/Flow
    //val friendGifts by remember { mutableStateOf(emptyList<Gift>()) }
    var friendFirebaseUid: String? by remember { mutableStateOf(null) }

    // Primeiro LaunchedEffect para buscar o UID e nome do amigo

    LaunchedEffect(friendEmail) {
          //  val friendFirebaseUid = appRepository.getFirebaseUserUidByEmail(friendEmail)
          //  if (friendFirebaseUid != null) {
            friendFirebaseUid = appRepository.getFirebaseUserUidByEmail(friendEmail)
            if (friendFirebaseUid != null) { // Somente atualiza se encontrar o UID
                    friendDisplayName = appRepository.getFirebaseUserNameByUid(friendFirebaseUid!!) ?: friendEmail
                    // Começa a coletar os presentes do amigo do Firestore
              //      appRepository.getFriendGiftsFromFirestore(friendFirebaseUid).collect { gifts ->
              //              (friendGifts as MutableState<List<Gift>>).value = gifts // Atualiza a lista de presentes
              //          }
                } else {
                    friendDisplayName = "Amigo não encontrado"
                }
    }


    // Segundo LaunchedEffect para coletar os presentes, que depende do friendFirebaseUid
    val friendGifts by (friendFirebaseUid?.let { appRepository.getFriendGiftsFromFirestore(it) } ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    
    Scaffold(
        topBar = {
            // Reutiliza o MainAppBar com um título dinâmico
         //   MainAppBar(title = "Presentes de ${friendEmail}", navController = navController)
            //MainAppBar(userName = userName, screenTitle = "Presentes de ${friendEmail}", navController = navController) // Atualizado
            MainAppBar(userName = userName, screenTitle = "Presentes de ${friendDisplayName}", navController = navController) // Atualizado
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
                //text = "Lista de Presentes de ${friendEmail}",
                text = "Lista de Presentes de ${friendDisplayName}",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Indicação de status de conectividade para ver presentes de amigos
            if (!appRepository.isOnline()) {
                Text(
                    text = "Você está offline. Os presentes de amigos são carregados quando você está online.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else if (friendFirebaseUid == null) {
                Text(
                    text = "Não foi possível encontrar o perfil do amigo no Firebase.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (friendGifts.isEmpty()) {
                    Text(
                            text = "Nenhum presente registrado para este amigo ainda.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                                )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                    Text(
                                            "Quero ganhar",
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                                )
                            }
                            items(friendGifts.filter { it.isWanted }) { gift ->
                                    Text("• ${gift.description}", style = MaterialTheme.typography.bodyMedium)
                            }
                            //Spacer(modifier = Modifier.height(16.dp))
                            item {
                                    Text(
                                            "Não quero ganhar",
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                }
                            items(friendGifts.filter { !it.isWanted }) { gift ->
                                    Text("• ${gift.description}", style = MaterialTheme.typography.bodyMedium)
                                }
                    }
                }
        }
    }
}

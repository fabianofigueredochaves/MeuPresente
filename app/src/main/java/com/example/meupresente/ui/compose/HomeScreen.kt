package com.example.meupresente.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
//import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
//import com.example.meupresente.MeuPresente
import com.example.meupresente.models.Gift
import com.example.meupresente.ViewModel.HomeViewModel
import com.example.meupresente.ViewModel.HomeViewModelFactory
import com.example.meupresente.ViewModel.MeuPresenteApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Long,
    navController: NavController
) {
    // --- Configuração do ViewModel ---
    val context = LocalContext.current
    val appRepository = (context.applicationContext as MeuPresenteApplication).repository
    val factory = HomeViewModelFactory(appRepository, userId)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)

    // --- Coleta dos Estados da UI ---
    val wishedGifts by homeViewModel.wishedGifts.collectAsState()
    val unwishedGifts by homeViewModel.unwishedGifts.collectAsState()
    var newGiftDescription by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Minha Lista de Presentes") },
                    actions = {
                        IconButton(onClick = {
                            // Lógica de Logoff
                            navController.navigate("login") {
                                popUpTo(0) // Limpa toda a pilha de navegação
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logoff")
                        }

                    }
                )
            }
        ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                    ) {
                        // --- Área para Adicionar Novo Presente ---
                        Text("Adicionar presente:", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                onValueChange = { newGiftDescription = it },
                                value = newGiftDescription,
                                label = { Text("O que você quer, ou não, ganhar?") },
                                    modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            homeViewModel.addGift(
                                                newGiftDescription,
                                                isWanted = true
                                            )
                                            newGiftDescription = "" // Limpa o campo após adicionar
                                        },
                                        enabled = newGiftDescription.isNotBlank()
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Adicionar presente")
                                    }

                                    IconButton(
                                        onClick = {
                                            homeViewModel.addGift(
                                                newGiftDescription,
                                                isWanted = false
                                            )
                                            newGiftDescription = "" // Limpa o campo após adicionar
                                        },
                                        enabled = newGiftDescription.isNotBlank()
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Evitar presente")
                                    }

                                }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // --- Listas de Presentes ---
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    // Lista de Presentes Desejados
                                    item {
                                        Text(
                                            "Quero ganhar",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    items(wishedGifts) { gift ->
                                        GiftItem(
                                            gift = gift,
                                            onToggleStatus = { homeViewModel.toggleGiftStatus(gift) },
                                            onDelete = { homeViewModel.deleteGift(gift) }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }

                                    // Lista de Presentes indesejados
                                    item {
                                        Text(
                                            "Não quero ganhar",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    items(unwishedGifts) { gift ->
                                        GiftItem(
                                            gift = gift,
                                            onToggleStatus = { homeViewModel.toggleGiftStatus(gift) },
                                            onDelete = { homeViewModel.deleteGift(gift) }
                                        )
                                    }
                                }
                        }
                    }
                }

                @Composable
                fun GiftItem(
                    gift: Gift,
                    onToggleStatus: () -> Unit,
                    onDelete: () -> Unit
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ícone para marcar/desmarcar
                            IconButton(onClick = onToggleStatus) {
                                Icon(
                                    imageVector = if (gift.isWanted) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = "Marcar como ganho/indesejado"
                                )
                            }

                            Text(
                                text = gift.description,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold
                            )

                            // Ícone para deletar
                            IconButton(onClick = onDelete) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Deletar presente",
                                tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

/*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meupresente.ViewModel.HomeViewModel
import com.example.meupresente.models.Gift

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meupresente.ViewModel.MeuPresenteApplication
import com.example.meupresente.ViewModel.HomeViewModelFactory // 👈 Importe a NOVA fábrica

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(userId: Long, navController: NavController) {
    // O ViewModel irá buscar esses dados do banco de dados
    val wishedGifts = listOf("Livro de Fantasia", "Fone de ouvido Bluetooth")
    val unwishedGifts = listOf("Meias", "Caneca genérica")
    var newGiftText by remember { mutableStateOf("FFC") }
    var newFriendEmail by remember { mutableStateOf("ffc@ffc.com") }

    // 1. Pegamos o contexto
    val context = LocalContext.current

    // 2. Acessamos o repositório da nossa classe Application
    val appRepository = (context.applicationContext as MeuPresenteApplication).repository

    // 3. Criamos a instância da HomeViewModelFactory, passando o repositório E o userId
    val factory = HomeViewModelFactory(appRepository, userId)

    // 4. E finalmente, passamos a fábrica para o construtor do viewModel()!
    val homeViewModel: HomeViewModel = viewModel(factory = factory)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Presente ��") },
                actions = {
                    IconButton(onClick = {
                        // Lógica de Logoff
                        navController.navigate("login") {
                            popUpTo(0) // Limpa toda a pilha de navegação
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logoff")
                    }

                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            // Seção para adicionar novos presentes
            OutlinedTextField(
                value = newGiftText,
                onValueChange = { newGiftText = it },
                label = { Text("O que você quer?") },
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                Button(onClick = { /* TODO: Chamar ViewModel para adicionar como "quero ganhar" */ }) {
                    Text("Quero Ganhar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { /* TODO: Chamar ViewModel para adicionar como "não quero ganhar" */ }) {
                    Text("Não Quero")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Listas de presentes
           // GiftListSection("O que eu quero ganhar ✅", wishedGifts)
            Spacer(modifier = Modifier.height(16.dp))
          //  GiftListSection("O que eu NÃO quero ganhar ❌", unwishedGifts)

            Spacer(modifier = Modifier.weight(1f)) // Empurra para baixo

            // Seção de Amigos
            Text("Adicionar Amigo", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = newFriendEmail,
                onValueChange = { newFriendEmail = it },
                label = { Text("Email do amigo") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { /* TODO: Chamar ViewModel para adicionar amigo */ }) {
                Text("Adicionar")
            }
        }
    }
}

@Composable
fun GiftListSection(title: String, gifts: List<Gift>, onDelete: (Gift) -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(gifts) { gift ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "• ${gift.description}", modifier = Modifier.padding(vertical = 4.dp))
                    IconButton(onClick = { onDelete(gift) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Deletar presente")
                    }
                }
            }
        }
    }
}

*/

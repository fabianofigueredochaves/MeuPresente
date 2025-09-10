package com.example.meupresente.ui.register


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType // Import para teclado numérico
import androidx.compose.foundation.text.KeyboardOptions // Import para opções de teclado
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.meupresente.ViewModel.MeuPresenteApplication
import com.example.meupresente.ViewModel.ViewModelFactory
import com.example.meupresente.ui.register.RegisterState // Import do RegisterState
import androidx.compose.runtime.LaunchedEffect // Import para LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope // Import para rememberCoroutineScope
import kotlinx.coroutines.launch // Import para launch
import androidx.compose.material3.SnackbarHost // Import para SnackbarHost
import androidx.compose.material3.SnackbarHostState // Import para SnackbarHostState
import com.example.meupresente.ui.components.DateInputVisualTransformation // Import para a transformação visual da data


@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() } // Estado para Snackbar
    val scope = rememberCoroutineScope() // Escopo para coroutines do Snackbar

    // 1. Pegamos o contexto atual
    val context = LocalContext.current

    // 2. Acessamos nossa classe Application através do contexto para pegar o repositório
    val appRepository = (context.applicationContext as MeuPresenteApplication).repository

    // 3. Criamos a instância da fábrica com o repositório
    val factory = ViewModelFactory(appRepository)

    // 4. Finalmente, passamos a fábrica para o construtor do viewModel()!
    val registerViewModel: RegisterViewModel = viewModel(factory = factory)
/*
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crie sua Conta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome Completo") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Senha") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = birthday, onValueChange = { birthday = it }, label = { Text("Data de Aniversário (DD/MM/AAAA)") })
        Spacer(modifier = Modifier.height(24.dp))


        Button(onClick = {
            // TODO: Chamar o ViewModel para registrar o usuário
            registerViewModel.registerUser(name, email, password, birthday) {

            navController.navigate("login") {
            popUpTo("login") { inclusive = true } } }
        }) {
            Text("Cadastrar")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Já tenho uma conta. Fazer Login")
        }
    }

 */

    // Coleta o estado do ViewModel para a UI reagir

    val registerState by registerViewModel.registerState.collectAsState()

    // Lógica para observar o estado do registro e navegar/mostrar erro
    LaunchedEffect(registerState) {
            when (registerState) {
                    is RegisterState.Success -> {
                            // Navega para a tela de login após o sucesso
                            navController.navigate("login") {
                                    // Limpa a pilha para que o usuário não possa voltar para o registro
                                    popUpTo("login") { inclusive = true }
                                }
                            registerViewModel.resetState() // Reseta o estado para evitar re-navegação
                        }
                    is RegisterState.Error -> {
                            scope.launch {
                                    snackbarHostState.showSnackbar((registerState as RegisterState.Error).message)
                                }
                            registerViewModel.resetState() // Reseta o estado para permitir novas tentativas
                        }
                    else -> { /* Idle ou Loading: não faz nada */ }
                }
        }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
            Column(
                    modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                    Text("Crie sua Conta", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome Completo") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                                )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                                )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Senha") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true
                                )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = birthday,
                            //onValueChange = { birthday = it },
                            onValueChange = { newValue ->
                            // Filtra apenas dígitos e limita o comprimento a 8 (para DDMMYYYY)
                                 val filteredValue = newValue.filter { it.isDigit() }
                                 if (filteredValue.length <= 8) {
                                    birthday = filteredValue
                                 }
                            },
                            label = { Text("Data de Aniversário (DD/MM/AAAA)") },
                            modifier = Modifier.fillMaxWidth(),
                          //  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                          //  singleLine = true
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), // Usamos NumberPassword para evitar sugestões do teclado e focar apenas em números.
                            singleLine = true,
                            visualTransformation = DateInputVisualTransformation() // Aplica a transformação visual


                                )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                            onClick = {
                             //   registerViewModel.registerUser(name, email, password, birthday)
                                // Formata a data para DD/MM/AAAA antes de enviar para o ViewModel
                                // A validação no ViewModel (`isValidDate`) espera o formato completo DD/MM/YYYY.
                                val formattedBirthday = if (birthday.length == 8) {
                                        "${birthday.substring(0, 2)}/${birthday.substring(2, 4)}/${birthday.substring(4, 8)}"
                                    } else {
                                        birthday // Se não tiver 8 dígitos, passa como está. O ViewModel irá tratar como inválido.
                                    }
                                registerViewModel.registerUser(name, email, password, formattedBirthday)

                                      },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = registerState !is RegisterState.Loading // Desabilita o botão durante o carregamento
                        ) {
                            if (registerState is RegisterState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text("Cadastrar")
                                }
                        }

                    TextButton(onClick = { navController.popBackStack() }) {
                            Text("Já tenho uma conta. Fazer Login")
                        }
                }
        }
}



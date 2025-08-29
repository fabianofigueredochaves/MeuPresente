package com.example.meupresente.ui.register


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.meupresente.ViewModel.MeuPresenteApplication
import com.example.meupresente.ViewModel.ViewModelFactory

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }

    // 1. Pegamos o contexto atual
    val context = LocalContext.current

    // 2. Acessamos nossa classe Application através do contexto para pegar o repositório
    val appRepository = (context.applicationContext as MeuPresenteApplication).repository

    // 3. Criamos a instância da fábrica com o repositório
    val factory = ViewModelFactory(appRepository)

    // 4. Finalmente, passamos a fábrica para o construtor do viewModel()!
    val registerViewModel: RegisterViewModel = viewModel(factory = factory)

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

}



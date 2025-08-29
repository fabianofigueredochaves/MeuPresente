@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.meupresente.ui.compose

//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meupresente.ViewModel.LoginState
import com.example.meupresente.ViewModel.LoginViewModel
import com.example.meupresente.ViewModel.MeuPresenteApplication
import com.example.meupresente.ViewModel.ViewModelFactory


//@OptIn(ExperimentalMaterial3Api::class)
//@Preview(name="login", showBackground = true)
//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController
) {
    // ---- Configuração do ViewModel ----
    val context = LocalContext.current
    val appRepository = (context.applicationContext as MeuPresenteApplication).repository
    val factory = ViewModelFactory(appRepository)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)

    // Coleta o estado do ViewModel para a UI reagir
    val loginState by loginViewModel.loginState.collectAsState()

    // ---- Estado da UI ----
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // ---- UI da Tela ----
    Scaffold(
        //topBar = {
        //    TopAppBar(title = { Text("Login") })
       // }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Meu Presente",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de E-mail
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Senha
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled
                    //Icons.Filled.Visibility
                    //else Icons.Filled.VisibilityOff
                    else Icons.Filled
                    val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        //  Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exibição de Erro
            if (loginState is LoginState.Error) {
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de Entrar
            Button(
                onClick = { loginViewModel.loginUser(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = loginState !is LoginState.Loading
            ) {
                if (loginState is LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Entrar")
                }
            }

            // Botão para ir para a tela de Registro
            TextButton(onClick = { navController.navigate("register") }) {
                Text("Não tem uma conta? Cadastre-se")
            }
        }
    }

    // ---- Lógica de Navegação e Efeitos ----
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val userId = (loginState as LoginState.Success).user.id
            //navController.navigate("home/$userId") {
            navController.navigate("dashboard/$userId") {
                // Limpa toda a pilha de navegação para que o usuário não possa voltar para o Login
                popUpTo(0)
            }
            loginViewModel.resetState() // Reseta o estado para evitar re-navegação
        }
    }
}
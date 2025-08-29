package com.example.meupresente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.meupresente.ui.compose.AppNavigation
import com.example.meupresente.ui.theme.MeuPresenteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Aplicamos o tema principal do aplicativo.
            // Isso garante que cores, fontes e formas sejam consistentes.
            MeuPresenteTheme {
                // Surface é um container básico que usa a cor de fundo do tema.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Aqui está a chamada!
                    // É aqui que a mágica da navegação começa.
                    // O AppNavigation() tomará controle da tela e exibirá
                    // a rota inicial que definimos (no nosso caso, "login").
                    AppNavigation()
                   // Text("Fabiano Figueredo Chaves")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MeuPresenteTheme {
        Greeting("Android")
    }
}
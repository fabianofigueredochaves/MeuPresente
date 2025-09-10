package com.example.meupresente.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
//import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.meupresente.ViewModel.MeuPresenteApplication
import com.example.meupresente.data.AppRepository
import com.example.meupresente.ui.register.RegisterScreen
import java.net.URLEncoder // Import para codificar o e-mail
import java.nio.charset.StandardCharsets // Import para codificar o e-mail

@Composable
fun AppNavigation() {
    //val navController = rememberNavController()
    //val repository = (LocalContext.current.applicationContext as MeuPresenteApplication).repository
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController) // Criaremos essa tela
        }
        composable("register") {
            RegisterScreen(navController = navController) // Criaremos essa tela
        }

        composable(
            route = "dashboard/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            DashboardScreen(userId = userId, navController = navController)
        }

        // A rota \"home\" agora é acessada a partir do Dashboard
        composable(
            route = "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            HomeScreen(userId = userId, navController = navController)
        }

        composable(
            route = "manage_friends/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->

            // 1. Obtemos o contexto aqui dentro do composable específico.
            val context = LocalContext.current
            // 2. Acessamos o repositório de forma segura.
            val repository = (context.applicationContext as MeuPresenteApplication).repository

            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            //ManageFriendsScreen(userId = userId, navController = navController)

            ManageFriendsScreen(
                userId = userId,
                navController = navController,
                repository = repository
            )
        }


        // Nova rota para a tela de presentes do amigo
        composable(
          //  route = "friend_gifts/{friendEmail}",
          //  arguments = listOf(navArgument("friendEmail") { type = NavType.StringType })
         route = "friend_gifts/{userId}/{friendEmail}", // Adiciona userId aqui
         arguments = listOf(
            navArgument("userId") { type = NavType.LongType }, // Parâmetro para o ID do usuário logado
            navArgument("friendEmail") { type = NavType.StringType }
         )

        ) { backStackEntry ->
           // val friendEmail = backStackEntry.arguments?.getString("friendEmail") ?: ""
           // FriendGiftsScreen(friendEmail = friendEmail, navController = navController)
           val userId = backStackEntry.arguments?.getLong("userId") ?: 0L // Pega o userId
           val friendEmail = backStackEntry.arguments?.getString("friendEmail") ?: ""
           FriendGiftsScreen(userId = userId, friendEmail = friendEmail, navController = navController)

        }


    }

}


        /*

        composable("home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull()
            if (userId != null) {
                 HomeScreen(userId = userId, navController = navController) // Criaremos essa tela
            }
        }
        */


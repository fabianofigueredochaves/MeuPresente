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


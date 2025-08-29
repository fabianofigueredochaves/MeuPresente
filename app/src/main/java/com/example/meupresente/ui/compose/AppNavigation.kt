package com.example.meupresente.ui.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
//import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.meupresente.ui.register.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController) // Criaremos essa tela
        }
        composable("register") {
            RegisterScreen(navController = navController) // Criaremos essa tela
        }

        composable(route = "dashboard/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            DashboardScreen(userId = userId, navController = navController)
        }

        // A rota \"home\" agora é acessada a partir do Dashboard
        composable(route = "home/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            HomeScreen(userId = userId, navController = navController)
        }


        /*

        composable("home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull()
            if (userId != null) {
                 HomeScreen(userId = userId, navController = navController) // Criaremos essa tela
            }
        }
        */

    }
}
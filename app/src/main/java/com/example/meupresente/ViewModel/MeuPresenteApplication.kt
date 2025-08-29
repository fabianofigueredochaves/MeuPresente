package com.example.meupresente.ViewModel

import android.app.Application
import androidx.room.Room
import com.example.meupresente.data.AppDatabase
import com.example.meupresente.data.AppRepository

class MeuPresenteApplication : Application() {

    // Usamos 'lazy' para que o banco de dados e o repositório
    // só sejam criados quando forem realmente necessários pela primeira vez.

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "meu_presente_database" // Nome do arquivo do banco de dados
        ).build()
    }

    val repository: AppRepository by lazy {
        AppRepository(database.appDAO())
    }
}
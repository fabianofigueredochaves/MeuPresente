// ARQUIVO: ./meupresente/ViewModel/MeuPresenteApplication.kt
package com.example.meupresente.ViewModel

import android.app.Application
//import androidx.compose.ui.unit.Constraints
import androidx.room.Room
import com.example.meupresente.data.AppDatabase
import com.example.meupresente.data.AppRepository
import androidx.work.Constraints // NOVO: Importe WorkManager
import androidx.work.NetworkType // NOVO: Importe WorkManager
import androidx.work.PeriodicWorkRequestBuilder // NOVO: Importe WorkManager
import androidx.work.WorkManager // NOVO: Importe WorkManager
import com.example.meupresente.workers.SyncWorker
import java.util.concurrent.TimeUnit // NOVO: Importe TimeUnit

class MeuPresenteApplication : Application() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "meu_presente_database"
        ).build()
    }

    // NOVO: Passe o applicationContext para o AppRepository
    val repository: AppRepository by lazy {
        AppRepository(database.appDAO(), applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        // Opcional: Configurar um WorkManager para sincronização periódica
        setupPeriodicSync()
    }

    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Só sincroniza se tiver conexão
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES, // Sincroniza a cada 15 minutos (ajuste conforme necessidade)
            5, TimeUnit.MINUTES // Flex time (tempo mínimo para começar depois do intervalo)
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MeuPresentePeriodicSync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Mantém o trabalho existente se já estiver agendado
            syncRequest
        )
    }
}

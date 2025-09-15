package com.example.meupresente.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.meupresente.ViewModel.MeuPresenteApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val application = applicationContext as MeuPresenteApplication
        val repository = application.repository
        val firebaseUser = repository.getCurrentFirebaseUser()
        val localUser = firebaseUser?.let { repository.getUserByFirebaseUid(it.uid) }

        if (localUser != null && firebaseUser != null && repository.isOnline()) {
            try {
                // Sincroniza dados do usuário local para o Firebase
                repository.syncLocalGiftsToFirestore(localUser.id, firebaseUser.uid)
                repository.syncLocalFriendshipsToFirestore(localUser.id, firebaseUser.uid)

                // Baixa dados do Firebase para o usuário local
                repository.fetchCurrentUserGiftsFromFirestoreToLocal(localUser.id, firebaseUser.uid)
                repository.fetchFriendDataFromFirestoreToLocal(localUser.id, firebaseUser.uid)

                Result.success()
            } catch (e: Exception) {
                // Logar o erro
                Result.retry() // Tentar novamente mais tarde
            }
        } else {
            // Não há usuário logado no Firebase ou está offline, ou usuário local não encontrado.
            // Não faz sentido sincronizar.
            Result.success() // Ou Result.failure() dependendo da política
        }
    }
}

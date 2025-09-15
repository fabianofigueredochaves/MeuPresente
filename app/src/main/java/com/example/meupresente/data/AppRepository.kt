// ARQUIVO: ./meupresente/data/AppRepository.kt
package com.example.meupresente.data

import android.content.Context // NOVO: Importe Context
import android.net.ConnectivityManager // NOVO: Importe ConnectivityManager
import android.net.NetworkCapabilities // NOVO: Importe NetworkCapabilities
import com.example.meupresente.models.Friendship
import com.example.meupresente.models.Gift
import com.example.meupresente.models.SyncStatus // NOVO: Importe o enum
import com.example.meupresente.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // NOVO: Para usar merge em Firestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first // NOVO: Para coletar o primeiro valor de um Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AppRepository(private val appDAO: AppDAO, private val context: Context) { // NOVO: Adicione Context ao construtor

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance() // Tornar público para LoginViewModel

    // --- Verificação de Conectividade ---
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    // --- Funções de Usuário ---
    suspend fun insertUser(user: User) {
        appDAO.insertUser(user)
    }

    suspend fun updateUser(user: User) { // NOVO: Implementar updateUser
        appDAO.updateUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return appDAO.getUserByEmail(email)
    }

    suspend fun getUserById(userId: Long): User? {
        return appDAO.getUserById(userId)
    }

    suspend fun getUserByFirebaseUid(firebaseUid: String): User? { // NOVO: Buscar usuário pelo UID do Firebase
        return appDAO.getUserByFirebaseUid(firebaseUid)
    }

    // --- Funções de Presente ---
    suspend fun insertGift(gift: Gift) {
        val giftToInsert = gift.copy(syncStatus = SyncStatus.PENDING_ADD)
        appDAO.insertGift(giftToInsert)
        if (isOnline() && getCurrentFirebaseUser() != null) {
            triggerSync() // Dispara a sincronização em segundo plano se online
        }
    }

    suspend fun updateGift(gift: Gift) {
        val giftToUpdate = gift.copy(syncStatus = SyncStatus.PENDING_UPDATE)
        appDAO.updateGift(giftToUpdate)
        if (isOnline() && getCurrentFirebaseUser() != null) {
            triggerSync()
        }
    }

    suspend fun deleteGift(gift: Gift) {
        // Marca o presente para exclusão pendente em vez de excluí-lo imediatamente
        appDAO.markGiftAsPendingDelete(gift.id)
        if (isOnline() && getCurrentFirebaseUser() != null) {
            triggerSync()
        }
    }

    fun getWishedGifts(userId: Long): Flow<List<Gift>> {
        return appDAO.getWishedGifts(userId)
    }

    fun getUnwishedGifts(userId: Long): Flow<List<Gift>> {
        return appDAO.getUnwishedGifts(userId)
    }

    // --- Funções de Autenticação Firebase ---
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    suspend fun registerUserWithFirebase(email: String, password: String): FirebaseUser? {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
    }

    suspend fun loginUserWithFirebase(email: String, password: String): FirebaseUser? {
        return firebaseAuth.signInWithEmailAndPassword(email, password).await().user
    }

    fun logoutFirebaseUser() {
        firebaseAuth.signOut()
    }

    // --- Funções de Firestore ---
    suspend fun saveUserToFirestore(user: User, firebaseUid: String) {
        val userMap = hashMapOf(
            "email" to user.email,
            "name" to user.name,
            "birthday" to user.birthday
        )
        firestore.collection("users").document(firebaseUid).set(userMap).await()
    }

    suspend fun getFirebaseUserUidByEmail(email: String): String? {
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        return querySnapshot.documents.firstOrNull()?.id
    }

    suspend fun getFirebaseUserNameByUid(uid: String): String? {
        val documentSnapshot = firestore.collection("users").document(uid).get().await()
        return documentSnapshot.getString("name")
    }

    // O método addGiftToFirestore é interno e será usado pela lógica de sincronização
    private suspend fun addGiftToFirestore(firebaseUid: String, gift: Gift) {
        val giftMap = hashMapOf(
            "description" to gift.description,
            "isWanted" to gift.isWanted,
            "timestamp" to System.currentTimeMillis() // Para ordenação
        )
        firestore.collection("users").document(firebaseUid).collection("gifts").add(giftMap).await()
    }

    // O método getFriendGiftsFromFirestore será usado para download de presentes de amigos
    fun getFriendGiftsFromFirestore(friendFirebaseUid: String): Flow<List<Gift>> =
        firestore.collection("users").document(friendFirebaseUid).collection("gifts")
            .orderBy("timestamp")
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.map { document ->
                    // NOVO: Adicionar firestoreId e syncStatus para consistência, mesmo que seja só para visualização remota
                    Gift(
                        id = 0, // ID local será 0, pois não persistimos no Room nesta etapa para presentes de amigos
                        userId = 0, // userId será 0 ou outro valor indicando que não é do usuário local
                        description = document.getString("description") ?: "",
                        isWanted = document.getBoolean("isWanted") ?: false,
                        firestoreId = document.id,
                        syncStatus = SyncStatus.SYNCED // Já vem sincronizado do Firebase
                    )
                }
            }

    // --- Funções de Amizade ---
    suspend fun addFriend(currentUserId: Long, friendEmail: String) {
        // Assume PENDING_ADD inicialmente. A sincronização tentará buscar os dados do amigo.
        val friendship = Friendship(userId = currentUserId, friendEmail = friendEmail.lowercase(), syncStatus = SyncStatus.PENDING_ADD)
        appDAO.addFriendship(friendship)
        if (isOnline() && getCurrentFirebaseUser() != null) {
            triggerSync()
        }
    }

    suspend fun removeFriend(currentUserId: Long, friendEmail: String) {
        // Marca a amizade para exclusão pendente
        appDAO.markFriendshipAsPendingDelete(currentUserId, friendEmail.lowercase())
        if (isOnline() && getCurrentFirebaseUser() != null) {
            triggerSync()
        }
    }

    fun getFriendships(currentUserId: Long): Flow<List<Friendship>> {
        return appDAO.getFriendships(currentUserId)
    }

    // --- Lógica de Sincronização ---
    private var isSyncing = false // Flag para evitar múltiplas sincronizações concorrentes

    suspend fun triggerSync() {
        if (isSyncing || !isOnline() || getCurrentFirebaseUser() == null) return

        isSyncing = true
        try {
            val firebaseUser = getCurrentFirebaseUser() ?: return
            val localUser = appDAO.getUserByFirebaseUid(firebaseUser.uid) ?: return // Assume que o usuário local já está vinculado ao Firebase

            // 1. Sincronizar presentes do usuário local para o Firebase
            syncLocalGiftsToFirestore(localUser.id, firebaseUser.uid)

            // 2. Sincronizar amizades locais para o Firebase e buscar dados de amigos
            syncLocalFriendshipsToFirestore(localUser.id, firebaseUser.uid)

            // 3. Buscar presentes do usuário do Firebase para o local (se adicionados em outro dispositivo)
            fetchCurrentUserGiftsFromFirestoreToLocal(localUser.id, firebaseUser.uid)

            // 4. Buscar presentes de amigos do Firebase para o local (apenas para exibição, se for o caso)
            // A sua `FriendGiftsScreen` já puxa diretamente do Firebase para os presentes dos amigos.
            // Se você quiser que os presentes dos amigos também sejam salvos localmente para visualização offline,
            // essa lógica precisaria ser adicionada aqui, possivelmente em uma tabela `FriendGift`.
            // Por enquanto, vamos manter o comportamento atual de FriendGiftsScreen.

        } finally {
            isSyncing = false
        }
    }

    suspend fun syncLocalGiftsToFirestore(userId: Long, firebaseUid: String) {
        val pendingGifts = appDAO.getPendingGiftsForUser(userId)

        for (gift in pendingGifts) {
            when (gift.syncStatus) {
                SyncStatus.PENDING_ADD -> {
                    try {
                        val docRef = firestore.collection("users").document(firebaseUid).collection("gifts").add(
                            hashMapOf(
                                "description" to gift.description,
                                "isWanted" to gift.isWanted,
                                "timestamp" to System.currentTimeMillis()
                            )
                        ).await()
                        // Atualiza o presente local com o firestoreId e marca como SYNCED
                        appDAO.updateGift(gift.copy(firestoreId = docRef.id, syncStatus = SyncStatus.SYNCED))
                    } catch (e: Exception) {
                        // Logar erro, tentar novamente mais tarde
                    }
                }
                SyncStatus.PENDING_UPDATE -> {
                    gift.firestoreId?.let { firestoreId ->
                        try {
                            firestore.collection("users").document(firebaseUid).collection("gifts").document(firestoreId).set(
                                hashMapOf(
                                    "description" to gift.description,
                                    "isWanted" to gift.isWanted
                                ),
                                SetOptions.merge() // Atualiza apenas os campos fornecidos
                            ).await()
                            appDAO.updateGift(gift.copy(syncStatus = SyncStatus.SYNCED))
                        } catch (e: Exception) {
                            // Logar erro
                        }
                    } ?: run {
                        // Se não tem firestoreId mas é PENDING_UPDATE, pode ser um item que falhou no PENDING_ADD.
                        // Tentar adicionar novamente.
                        appDAO.updateGift(gift.copy(syncStatus = SyncStatus.PENDING_ADD))
                        // Ou relatar erro, dependendo da política de resolução de conflitos.
                    }
                }
                SyncStatus.PENDING_DELETE -> {
                    gift.firestoreId?.let { firestoreId ->
                        try {
                            firestore.collection("users").document(firebaseUid).collection("gifts").document(firestoreId).delete().await()
                            appDAO.deleteGift(gift) // Exclui do Room após sucesso no Firebase
                        } catch (e: Exception) {
                            // Logar erro
                        }
                    } ?: appDAO.deleteGift(gift) // Se não tem firestoreId, só exclui localmente
                }
                SyncStatus.SYNCED -> {} // Não deve acontecer, pois estamos buscando itens != SYNCED
            }
        }
    }

    suspend fun syncLocalFriendshipsToFirestore(userId: Long, firebaseUid: String) {
        val pendingFriendships = appDAO.getPendingFriendshipsForUser(userId)

        for (friendship in pendingFriendships) {
            when (friendship.syncStatus) {
                SyncStatus.PENDING_ADD -> {
                    try {
                        val friendFirebaseUid = getFirebaseUserUidByEmail(friendship.friendEmail)
                        if (friendFirebaseUid != null) {
                            val friendName = getFirebaseUserNameByUid(friendFirebaseUid)
                            // Salva a amizade no Firebase (do ponto de vista do usuário atual)
                            firestore.collection("users").document(firebaseUid).collection("friends").document(friendFirebaseUid).set(
                                hashMapOf(
                                    "email" to friendship.friendEmail,
                                    "name" to friendName // Nome do amigo para referência rápida
                                    // Adicionar "birthday" se disponível publicamente no Firestore do amigo
                                )
                            ).await()
                            // Atualiza a amizade local com os dados do Firebase e marca como SYNCED
                            appDAO.updateFriendship(
                                friendship.copy(
                                    friendName = friendName,
                                    friendFirebaseUid = friendFirebaseUid,
                                    syncStatus = SyncStatus.SYNCED
                                )
                            )
                        } else {
                            // Se o amigo não foi encontrado no Firebase, remove localmente (ou marca como inválido)
                            appDAO.deleteFriendship(userId, friendship.friendEmail)
                        }
                    } catch (e: Exception) {
                        // Logar erro
                    }
                }
                SyncStatus.PENDING_DELETE -> {
                    friendship.friendFirebaseUid?.let { friendUid ->
                        try {
                            firestore.collection("users").document(firebaseUid).collection("friends").document(friendUid).delete().await()
                            appDAO.deleteFriendship(userId, friendship.friendEmail) // Exclui do Room
                        } catch (e: Exception) {
                            // Logar erro
                        }
                    } ?: appDAO.deleteFriendship(userId, friendship.friendEmail) // Se não tem UID, só exclui localmente
                }
                SyncStatus.SYNCED, SyncStatus.PENDING_UPDATE -> {} // Amizades não teriam PENDING_UPDATE se só adicionamos/removemos
            }
        }
    }

    // NOVO: Buscar presentes do próprio usuário do Firebase para o Room
    suspend fun fetchCurrentUserGiftsFromFirestoreToLocal(userId: Long, firebaseUid: String) {
        try {
            val firestoreGiftsSnapshot = firestore.collection("users").document(firebaseUid).collection("gifts").get().await()
            val firestoreGifts = firestoreGiftsSnapshot.documents.mapNotNull { doc ->
                val description = doc.getString("description")
                val isWanted = doc.getBoolean("isWanted")
                if (description != null && isWanted != null) {
                    Gift(userId = userId, description = description, isWanted = isWanted, firestoreId = doc.id, syncStatus = SyncStatus.SYNCED)
                } else null
            }

            val localWished = appDAO.getWishedGifts(userId).first() // Obtém a lista atual do Flow
            val localUnwished = appDAO.getUnwishedGifts(userId).first()
            val allLocalSyncedGifts = (localWished + localUnwished).filter { it.syncStatus == SyncStatus.SYNCED } // Apenas os já sincronizados

            for (firestoreGift in firestoreGifts) {
                val existingLocalGift = allLocalSyncedGifts.firstOrNull { it.firestoreId == firestoreGift.firestoreId }
                if (existingLocalGift == null) {
                    // Novo presente do Firebase, insere localmente
                    appDAO.insertGift(firestoreGift.copy(id = 0)) // Room gerará um novo ID
                } else if (existingLocalGift != firestoreGift) {
                    // Presente existe localmente e é diferente (assumindo que a igualdade é por conteúdo)
                    appDAO.updateGift(firestoreGift.copy(id = existingLocalGift.id))
                }
            }

            // Lógica para deletar localmente presentes que foram deletados no Firebase
            val firestoreGiftIds = firestoreGifts.map { it.firestoreId }
            allLocalSyncedGifts.forEach { localGift ->
                if (localGift.firestoreId != null && !firestoreGiftIds.contains(localGift.firestoreId)) {
                    appDAO.deleteGift(localGift) // Exclui localmente
                }
            }

        } catch (e: Exception) {
            // Logar erro
        }
    }

    // NOVO: Buscar dados de registro de amigos do Firebase para o Room
    suspend fun fetchFriendDataFromFirestoreToLocal(userId: Long, firebaseUid: String) {
        try {
            val localFriendships = appDAO.getFriendships(userId).first()

            for (friendship in localFriendships) {
                // Se faltam informações (nome, UID do Firebase) ou a amizade ainda não foi sincronizada
                if (friendship.friendFirebaseUid == null || friendship.friendName == null || friendship.birthday == null || friendship.syncStatus != SyncStatus.SYNCED) {
                    val friendFirebaseUid = getFirebaseUserUidByEmail(friendship.friendEmail)
                    if (friendFirebaseUid != null) {
                        val firestoreDoc = firestore.collection("users").document(friendFirebaseUid).get().await()
                        val name = firestoreDoc.getString("name")
                        val birthday = firestoreDoc.getString("birthday")

                        if (name != null || birthday != null) {
                            appDAO.updateFriendship(
                                friendship.copy(
                                    friendFirebaseUid = friendFirebaseUid,
                                    friendName = name ?: friendship.friendName,
                                    birthday = birthday ?: friendship.birthday,
                                    syncStatus = SyncStatus.SYNCED
                                )
                            )
                        }
                    } else {
                        // Se o amigo não existe no Firebase, remover localmente ou marcar como inválido
                        appDAO.deleteFriendship(userId, friendship.friendEmail)
                    }
                }
            }
        } catch (e: Exception) {
            // Logar erro
        }
    }

    // O método performFullSync será o ponto de entrada para a sincronização completa
    // Ele será chamado após o login bem-sucedido ou quando a conectividade for restaurada
    suspend fun performFullSync(currentUserId: Long, firebaseUid: String) {
        if (!isOnline()) return // Só sincroniza se estiver online

        val localUser = appDAO.getUserById(currentUserId)
        if (localUser != null && localUser.firebaseUid == null) {
            // Se o usuário local ainda não tem o firebaseUid, atualize-o
            appDAO.updateUser(localUser.copy(firebaseUid = firebaseUid))
        }

        triggerSync() // Inicia o processo de sincronização completo
    }
}

package com.example.meupresente.data

import androidx.room.*
import com.example.meupresente.models.Friendship
import com.example.meupresente.models.Gift
import com.example.meupresente.models.SyncStatus // NOVO: Importe o enum
import com.example.meupresente.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDAO {
    // --- Funções de Usuário ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    @Update // NOVO: Adicionar método para atualizar usuário (necessário para firebaseUid)
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users WHERE firebaseUid = :firebaseUid LIMIT 1") // NOVO: Buscar usuário pelo UID do Firebase
    suspend fun getUserByFirebaseUid(firebaseUid: String): User?

    // --- Funções de Presente ---
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Use REPLACE para gifts que podem ser atualizados
    suspend fun insertGift(gift: Gift)

    @Query("SELECT * FROM gifts WHERE userId = :userId AND isWanted = 1 AND syncStatus != :pendingDeleteStatus") // NOVO: Excluir itens com PENDING_DELETE da lista
    fun getWishedGifts(userId: Long, pendingDeleteStatus: SyncStatus = SyncStatus.PENDING_DELETE): Flow<List<Gift>>

    @Query("SELECT * FROM gifts WHERE userId = :userId AND isWanted = 0 AND syncStatus != :pendingDeleteStatus") // NOVO: Excluir itens com PENDING_DELETE da lista
    fun getUnwishedGifts(userId: Long, pendingDeleteStatus: SyncStatus = SyncStatus.PENDING_DELETE): Flow<List<Gift>>

    @Update
    suspend fun updateGift(gift: Gift)

    // NOVO: Marcar presente para exclusão pendente em vez de excluir imediatamente
    @Query("UPDATE gifts SET syncStatus = :pendingDeleteStatus WHERE id = :giftId")
    suspend fun markGiftAsPendingDelete(giftId: Long, pendingDeleteStatus: SyncStatus = SyncStatus.PENDING_DELETE)

    @Delete
    suspend fun deleteGift(gift: Gift) // Exclusão real após a sincronização

    @Query("SELECT * FROM gifts WHERE userId = :userId AND syncStatus != :syncedStatus") // NOVO: Obter presentes com status de sincronização pendente
    suspend fun getPendingGiftsForUser(userId: Long, syncedStatus: SyncStatus = SyncStatus.SYNCED): List<Gift>

    // --- Funções de Amizade ---

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Use REPLACE para amizades que podem ser atualizadas (ex: preencher nome, aniversário)
    suspend fun addFriendship(friendship: Friendship)

    @Update // NOVO: Adicionar método para atualizar amizade
    suspend fun updateFriendship(friendship: Friendship)

    // NOVO: Marcar amizade para exclusão pendente
    @Query("UPDATE friendships SET syncStatus = :pendingDeleteStatus WHERE userId = :userId AND friendEmail = :friendEmail")
    suspend fun markFriendshipAsPendingDelete(userId: Long, friendEmail: String, pendingDeleteStatus: SyncStatus = SyncStatus.PENDING_DELETE)

    @Query("DELETE FROM friendships WHERE userId = :userId AND friendEmail = :friendEmail")
    suspend fun deleteFriendship(userId: Long, friendEmail: String) // Exclusão real após a sincronização

    @Query("SELECT * FROM friendships WHERE userId = :userId AND syncStatus != :pendingDeleteStatus") // NOVO: Excluir itens com PENDING_DELETE da lista de visualização
    fun getFriendships(userId: Long, pendingDeleteStatus: SyncStatus = SyncStatus.PENDING_DELETE): Flow<List<Friendship>>

    @Query("SELECT * FROM friendships WHERE userId = :userId AND syncStatus != :syncedStatus") // NOVO: Obter amizades com status de sincronização pendente
    suspend fun getPendingFriendshipsForUser(userId: Long, syncedStatus: SyncStatus = SyncStatus.SYNCED): List<Friendship>

    // Seu método searchUsers permanece o mesmo se a busca for apenas local
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' AND id != :currentUserId LIMIT 20")
    suspend fun searchUsers(query: String, currentUserId: Long): List<User>
}

package com.example.meupresente.data

import androidx.room.*
import com.example.meupresente.models.Friendship
import com.example.meupresente.models.Gift
import com.example.meupresente.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDAO {
    // --- Funções de Usuário ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    // --- Funções de Presente ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGift(gift: Gift)

    @Query("SELECT * FROM gifts WHERE userId = :userId AND isWanted = 1")
    fun getWishedGifts(userId: Long): Flow<List<Gift>>

    @Query("SELECT * FROM gifts WHERE userId = :userId AND isWanted = 0")
    fun getUnwishedGifts(userId: Long): Flow<List<Gift>>

    @Update
    suspend fun updateGift(gift: Gift)

    @Delete
    suspend fun deleteGift(gift: Gift)

    // --- Funções de Amizade ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFriend(friendship: Friendship)

    /**
     * Busca todos os usuários cujo nome contém o texto da busca.
     * O 'LIMIT 20' é uma boa prática para não carregar resultados demais.
     * O '||' é o operador de concatenação do SQL, para o 'LIKE' funcionar.
     */
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' AND id != :currentUserId LIMIT 20")
    suspend fun searchUsers(query: String, currentUserId: Long): List<User>

    /**
     * Insere uma nova relação de amizade.
     */
  //  @Insert
 //   suspend fun addFriendship(friendship: Friendship)

    /**
     * Remove uma relação de amizade.
     * Note que precisamos checar ambas as direções da amizade (user1 -> user2 e user2 -> user1).
     */
    @Query("DELETE FROM friendships WHERE (userId = :user1Id AND userId = :user2Id) OR (userId = :user2Id AND userId = :user1Id)")
    fun removeFriendship(user1Id: Long, user2Id: Long)

    /**
     * Verifica se uma amizade específica existe entre dois usuários.
     * Retorna 1 (true) se existir, 0 (false) se não.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM friendships WHERE (userId = :user1Id AND userId = :user2Id) OR (userId = :user2Id AND userId = :user1Id)) LIMIT 1")
    suspend fun friendshipExists(user1Id: Long, user2Id: Long): Boolean



}
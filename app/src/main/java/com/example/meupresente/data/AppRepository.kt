package com.example.meupresente.data

import com.example.meupresente.models.Friendship
import com.example.meupresente.models.Gift
import com.example.meupresente.models.User
import kotlinx.coroutines.flow.Flow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await // Para usar .await() em tarefas Firebase

// O repositório precisa do DAO para acessar o banco de dados.
// Recebemos o DAO através do construtor.
class AppRepository(private val appDAO: AppDAO) {

 //   private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    // Funções de Usuário
    suspend fun insertUser(user: User) {
        appDAO.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return appDAO.getUserByEmail(email)
    }
    suspend fun getUserById(userId: Long): User? {
        return appDAO.getUserById(userId)
    }

    // Funções de Presente
    suspend fun insertGift(gift: Gift) {
        appDAO.insertGift(gift)
    }

    suspend fun updateGift(gift: Gift) {
        appDAO.updateGift(gift)
    }

    suspend fun deleteGift(gift: Gift) {
        appDAO.deleteGift(gift)
    }


    // Usamos Flow para que a UI receba atualizações automaticamente
    fun getWishedGifts(userId: Long): Flow<List<Gift>> {
        return appDAO.getWishedGifts(userId)
    }

    fun getUnwishedGifts(userId: Long): Flow<List<Gift>> {
        return appDAO.getUnwishedGifts(userId)
    }


/*
    // --- Funções de Autenticação Firebase ---

    // Retorna o usuário Firebase logado atualmente
    fun getCurrentFirebaseUser(): FirebaseUser? {
            return firebaseAuth.currentUser
        }

    // Cadastra um novo usuário no Firebase Auth
    suspend fun registerUserWithFirebase(email: String, password: String): FirebaseUser? {
            return firebaseAuth.createUserWithEmailAndPassword(email, password).result?.user
        }

    // Loga um usuário no Firebase Auth
    suspend fun loginUserWithFirebase(email: String, password: String): FirebaseUser? {
            return firebaseAuth.signInWithEmailAndPassword(email, password).result?.user
        }

    // Faz logout do usuário do Firebase Auth
    fun logoutFirebaseUser() {
            firebaseAuth.signOut()
        }
   */
    // Funções de Amizade

    suspend fun searchUsers(query: String, currentUserId: Long) = appDAO.searchUsers(query, currentUserId)

    suspend fun addFriend(currentUserId: Long, friendEmail: String) {
        val friendship = Friendship(userId = currentUserId, friendEmail = friendEmail, friendName = "", birthday = "")
        appDAO.addFriendship(friendship)
    }


    suspend fun removeFriend(currentUserId: Long, friendEmail: String) {
        appDAO.removeFriendship(currentUserId, friendEmail)
    }

    fun getFriendships(currentUserId: Long): Flow<List<Friendship>> {
        return appDAO.getFriendships(currentUserId)
    }

/*
    suspend fun friendshipExists(currentUserId: Long, friendId: Long): Boolean {
        return appDAO.friendshipExists(currentUserId, friendId)
    }
*/


}
/*
private fun AppDao.addFriend(friendship: Friendship) {}

private fun AppDao.getUnwishedGifts(userId: Long): Flow<List<Gift>> {}

private fun AppDao.getWishedGifts(userId: Long): Flow<List<Gift>> {}

private fun AppDao.deleteGift(gift: Gift) {}

private fun AppDao.insertGift(gift: Gift) {}

private fun AppDao.getUserByEmail(email: String): User? {}

private fun AppDao.insertUser(user: User) {}

annotation class AppDao
*/
package com.example.meupresente.data


import com.example.meupresente.models.Friendship
import com.example.meupresente.models.Gift
import com.example.meupresente.models.User
import kotlinx.coroutines.flow.Flow

// O repositório precisa do DAO para acessar o banco de dados.
// Recebemos o DAO através do construtor.
class AppRepository(private val appDAO: AppDAO) {

    // Funções de Usuário
    suspend fun insertUser(user: User) {
        appDAO.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return appDAO.getUserByEmail(email)
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

    // Funções de Amizade
    suspend fun addFriend(friendship: Friendship) {
        appDAO.addFriend(friendship)
    }
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
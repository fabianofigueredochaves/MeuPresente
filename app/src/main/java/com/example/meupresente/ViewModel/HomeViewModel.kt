package com.example.meupresente.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.Friendship
import com.example.meupresente.models.Gift
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: AppRepository,
    private val userId: Long // O ID do usuário logado
) : ViewModel() {

    // Expõe as listas de presentes como StateFlow.
    // A UI vai observar esses Flows e se atualizar automaticamente.

    val wishedGifts: StateFlow<List<Gift>> = repository.getWishedGifts(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Começa a coletar quando a UI está visível
            initialValue = emptyList() // Valor inicial enquanto os dados carregam
        )

    val unwishedGifts: StateFlow<List<Gift>> = repository.getUnwishedGifts(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addGift(description: String, isWanted: Boolean) {
        if (description.isBlank()) return

        viewModelScope.launch {
            val newGift = Gift(
                // id = 0 para que o Room gere um ID automaticamente
                id = 0,
                description = description,
                isWanted = isWanted, // 'true' porque é um presente que o usuário \"quer ganhar\"
                userId = userId // Associa o presente ao usuário logado
            )
            repository.insertGift(newGift)
        }
    }

    fun deleteGift(gift: Gift) {
        viewModelScope.launch {
            repository.deleteGift(gift)
        }
    }

    fun addFriend(friendEmail: String) {
        if (friendEmail.isBlank()) return

        viewModelScope.launch {
            /*
            val friendship = Friendship(
                userId = userId,
                friendEmail = friendEmail.lowercase()
            )
            // repository.addFriend(friendship)
            */
            repository.addFriend(userId, friendEmail.lowercase())

        }
    }

    fun toggleGiftStatus(gift: Gift) {
        viewModelScope.launch {
            // Cria uma cópia do presente, invertendo o status 'isWanted'
            val updatedGift = gift.copy(isWanted = !gift.isWanted)
            repository.updateGift(updatedGift)
        }
    }

}


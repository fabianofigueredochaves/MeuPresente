package com.example.meupresente.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map // Importar o operador map
import android.util.Patterns // Import para validação de e-mail
import com.example.meupresente.models.Friendship

// Eventos para a UI (ex: SnackBar)
sealed class ManageFriendsEvent {
    data class ShowMessage(val message: String) : ManageFriendsEvent()
}

class ManageFriendsViewModel(
    private val repository: AppRepository,
    private val currentUserId: Long
) : ViewModel() {

    // --- Estado para o campo de input de e-mail ---
    private val _friendEmailInput = MutableStateFlow("")
    val friendEmailInput = _friendEmailInput.asStateFlow()

    fun onFriendEmailInputChange(email: String) {
        _friendEmailInput.value = email
    }

    // --- 👇 MUDANÇA MAIS IMPORTANTE AQUI 👇 ---
    // Agora expomos uma lista de Strings (emails), em vez de objetos User.
    //val friends: StateFlow<List<String>> = repository.getFriendships(currentUserId)
     //   .map { friendships -> // Usamos o operador map para transformar a lista de Friendship
     //       friendships.map { it.friendEmail } // Extraímos apenas o friendEmail de cada Friendship
     //   }
    val friends: StateFlow<List<Friendship>> = repository.getFriendships(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    // --- FIM DA MUDANÇA MAIS IMPORTANTE ---


    // --- Eventos de mensagens (ex: para mostrar um SnackBar) ---
    private val _event = MutableSharedFlow<ManageFriendsEvent>()
    val event = _event.asSharedFlow()

    // --- Funções para adicionar e remover amigos ---

    fun addFriend() {
        val emailToAdd = _friendEmailInput.value.trim().lowercase()

        viewModelScope.launch {
            if (emailToAdd.isBlank()) {
                _event.emit(ManageFriendsEvent.ShowMessage("O e-mail não pode estar em branco."))
                return@launch
            }

            if (!isValidEmail(emailToAdd)) {
                    _event.emit(ManageFriendsEvent.ShowMessage("Formato de e-mail de amigo inválido."))
                    return@launch
            }

            // Verifica se o e-mail do amigo existe no Firebase (se ele se registrou no app)
            val friendFirebaseUid = repository.getFirebaseUserUidByEmail(emailToAdd)
            if (friendFirebaseUid == null) {
                    _event.emit(ManageFriendsEvent.ShowMessage("Usuário com este e-mail não encontrado no aplicativo."))
                    return@launch
            }
            

            // Evitar adicionar a si mesmo
            val currentUser = repository.getUserById(currentUserId) // Seu TXT tem getUserById
            if (currentUser != null && emailToAdd == currentUser.email.lowercase()) {
                _event.emit(ManageFriendsEvent.ShowMessage("Você não pode adicionar a si mesmo como amigo(a)."))
                return@launch
            }
            /*

            // Precisamos verificar se o e-mail existe no banco de dados geral de usuários
            val friendUser = repository.getUserByEmail(emailToAdd) // Seu TXT tem getUserByEmail
            if (friendUser == null) {
                _event.emit(ManageFriendsEvent.ShowMessage("Usuário com este e-mail não encontrado."))
                return@launch
            }
            */

            // Verificar se já é amigo(a)
            val currentFriendships = repository.getFriendships(currentUserId).first() // Pega a lista atual uma vez
            if (currentFriendships.any { it.friendEmail.equals(emailToAdd, ignoreCase = true) }) {
                // Aqui, como não temos o nome do amigo ainda, usamos o email ou um placeholder
                //_event.emit(ManageFriendsEvent.ShowMessage("Você já é amigo(a) de ${"friendUser.name" ?: emailToAdd}."))
              //  val friendName = repository.getFirebaseUserNameByUid(friendFirebaseUid) ?: emailToAdd
                //_event.emit(ManageFriendsEvent.ShowMessage("Você já é amigo(a) de ${friendName}."))
                _event.emit(ManageFriendsEvent.ShowMessage("Você já é amigo(a) de ${emailToAdd}."))
                return@launch
            }

            repository.addFriend(currentUserId, emailToAdd)
            _friendEmailInput.value = "" // Limpa o campo após sucesso
            // Feedback mais genérico, já que não garantimos o nome do amigo neste momento
            //_event.emit(ManageFriendsEvent.ShowMessage("Amigo ${"friendUser.name" ?: emailToAdd} adicionado(a)!"))
            val friendName = repository.getFirebaseUserNameByUid(friendFirebaseUid) ?: emailToAdd
            _event.emit(ManageFriendsEvent.ShowMessage("Amigo ${friendName} adicionado(a)!"))
        }
    }

    // --- Função Auxiliar de Validação ---
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun removeFriend(friendEmail: String) {
        viewModelScope.launch {
            repository.removeFriend(currentUserId, friendEmail.lowercase())
            // Feedback mais genérico
            val removedFriendDisplay = repository.getUserByEmail(friendEmail)?.name ?: friendEmail // Tenta pegar o nome se existir
            _event.emit(ManageFriendsEvent.ShowMessage("${removedFriendDisplay} removido(a) dos seus amigos."))
        }
    }
}
package com.example.meupresente.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.User
import kotlinx.coroutines.launch
import java.security.MessageDigest // Import para hashing
import java.text.SimpleDateFormat // Import para formatação de data
import java.util.Date // Import para formatação de data
import java.util.Locale // Import para formatação de data
import android.util.Patterns // Import para validação de e-mail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class RegisterState {
    data object Idle : RegisterState()
    data object Loading : RegisterState()
    data object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(private val repository: AppRepository) : ViewModel() {
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun registerUser(name: String, email: String, password: String, birthday: String) {
        _registerState.value = RegisterState.Loading

        // 1. Validação de Entrada
        if (name.isBlank() || email.isBlank() || password.isBlank() || birthday.isBlank()) {
            _registerState.value = RegisterState.Error("Todos os campos são obrigatórios.")
            return
        }
        if (!isValidEmail(email)) {
            _registerState.value = RegisterState.Error("Formato de e-mail inválido.")
            return
        }
        if (!isValidDate(birthday)) {
            _registerState.value =
                RegisterState.Error("Formato de data de nascimento inválido. Use DD/MM/AAAA.")
            return
        }
        // 2. Formatação do Nome
        val formattedName = name
            .filter { it.isLetter() || it.isWhitespace() } // Remove números e outros caracteres que não sejam letras ou espaços
            .lowercase(Locale.ROOT) // Converte tudo para minúsculas
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } // Primeira letra maiúscula
            .trim() // Remove espaços em branco extras no início/fim

        // Se o nome formatado ficar em branco (ex: só tinha números), ou se houver regras mais estritas.
        if (formattedName.isEmpty()) {
            _registerState.value = RegisterState.Error("Nome inválido.")
            return
        }

        viewModelScope.launch {
            try {
                // 3. Verifica se o usuário já existe
                if (repository.getUserByEmail(email) != null) {
                    _registerState.value = RegisterState.Error("Este e-mail já está cadastrado.")
                    return@launch
                }

                // 4. Hashing da Senha
                val hashedPassword = hashString(password)

                // 5. Criação e Inserção do Usuário
                val newUser = User(
                    name = formattedName,
                    email = email.lowercase(Locale.ROOT), // Salvar email em minúsculas para consistência
                    passwordHash = hashedPassword,
                    birthday = birthday // A data já foi validada
                )
                repository.insertUser(newUser)
                _registerState.value = RegisterState.Success

            } catch (e: Exception) {
                _registerState.value =
                    RegisterState.Error("Ocorreu um erro inesperado: ${e.message}")
            }
        }
    }

    // --- Funções Auxiliares de Validação e Hashing ---
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidDate(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.isLenient = false // Não permite datas inválidas como 30/02/2023
            format.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun hashString(input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }
        return result.toString()
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

/*

class RegisterViewModel(private val repository: AppRepository) : ViewModel() {

    fun registerUser(name: String, email: String, password: String, birthday: String, onRegistrationComplete: () -> Unit) {
        // Validações básicas (você pode expandir isso)
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            // Aqui você poderia expor um estado de erro para a UI
            return
        }

        viewModelScope.launch {
            // Primeiro, verifica se o usuário já existe
            if (repository.getUserByEmail(email) == null) {
                // TODO: Criptografar a senha antes de salvar!
                val newUser = User(
                    name = name,
                    email = email.lowercase(), // Salvar email em minúsculas para consistência
                    passwordHash = password, // Lembre-se de substituir por um hash real
                    birthday = birthday
                )
                repository.insertUser(newUser)
                onRegistrationComplete() // Chama o callback para navegar para outra tela
            } else {
                // Usuário já existe, informe a UI sobre o erro
            }
        }
    }
}

 */
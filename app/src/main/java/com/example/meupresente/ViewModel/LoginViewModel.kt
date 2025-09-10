package com.example.meupresente.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.User // Importe seu modelo User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest // Import para hashing
import android.util.Patterns // Import para validação de e-mail

// Estado que a UI vai observar
sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val repository: AppRepository) : ViewModel() {

    // _loginState é privado e mutável, apenas o ViewModel pode alterá-lo.
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)

    // loginState é público e imutável (StateFlow), a UI apenas o lê.
    val loginState: StateFlow<LoginState> = _loginState

    /**
     * Tenta autenticar o usuário com o email e senha fornecidos.
     */
    fun loginUser(email: String, password: String) {
        // Validação básica de entrada
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("E-mail e senha não podem estar em branco.")
            return
        }

        // Define o estado como Carregando antes de iniciar a operação de rede/banco
        _loginState.value = LoginState.Loading

        // Inicia uma coroutine no escopo do ViewModel
        viewModelScope.launch {
            try {
                // 1. Validação de E-mail
                if (!isValidEmail(email)) {
                    _loginState.value = LoginState.Error("Formato de e-mail inválido.")
                    return@launch
                    }

                // Busca o usuário pelo e-mail usando o repositório
                val user = repository.getUserByEmail(email)

                // Verifica se o usuário foi encontrado e se a senha corresponde
            //    if (user != null && user.passwordHash == password) {
                // 2. Hashing da senha digitada para comparação
                val hashedPasswordInput = hashString(password)
                if (user != null && user.passwordHash == hashedPasswordInput) { // Compara os hashes
                    _loginState.value = LoginState.Success(user)
                } else {
                    _loginState.value = LoginState.Error("E-mail ou senha inválidos.")
                }
            } catch (e: Exception) {
                // Captura qualquer outra exceção que possa ocorrer
                _loginState.value = LoginState.Error("Ocorreu um erro inesperado: ${e.message}")
            }
        }
    }

    /**
     * Reseta o estado para Idle, útil para quando a UI navega para outra tela.
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
  
    // --- Funções Auxiliares ---
    private fun isValidEmail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
    
    
}
package com.example.meupresente.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.User // Importe seu modelo User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
                // Busca o usuário pelo e-mail usando o repositório
                val user = repository.getUserByEmail(email)

                // Verifica se o usuário foi encontrado e se a senha corresponde
                if (user != null && user.passwordHash == password) {
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
}
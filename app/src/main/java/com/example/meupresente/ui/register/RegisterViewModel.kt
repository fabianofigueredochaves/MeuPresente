package com.example.meupresente.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.User
import kotlinx.coroutines.launch

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
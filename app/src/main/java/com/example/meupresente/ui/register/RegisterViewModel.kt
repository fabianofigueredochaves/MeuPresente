// Corrigir linha 80
package com.example.meupresente.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.User
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Patterns
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

        if (name.isBlank() || email.isBlank() || password.isBlank() || birthday.isBlank()) {
            _registerState.value = RegisterState.Error("Todos os campos são obrigatórios.")
            return
        }
        if (!isValidEmail(email)) {
            _registerState.value = RegisterState.Error("Formato de e-mail inválido.")
            return
        }
        if (!isValidDate(birthday)) {
            _registerState.value = RegisterState.Error("Formato de data de nascimento inválido. Use DD/MM/AAAA.")
            return
        }

        val formattedName = name
            .filter { it.isLetter() || it.isWhitespace() }
            .lowercase(Locale.ROOT)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            .trim()

        if (formattedName.isEmpty()) {
            _registerState.value = RegisterState.Error("Nome inválido.")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Tenta cadastrar o usuário no Firebase Authentication
                val firebaseUser = repository.registerUserWithFirebase(email, password)
                if (firebaseUser == null) {
                    _registerState.value = RegisterState.Error("Falha ao cadastrar no Firebase Auth. E-mail já em uso ou erro de servidor.")
                    return@launch
                }

                // 2. Se o cadastro no Firebase Auth foi bem-sucedido, insere no DB local
                val hashedPassword = hashString(password)
                val newUser = User(
                    firebaseUid = firebaseUser.uid, // NOVO: Salva o Firebase UID localmente
                    name = formattedName,
                    email = email.lowercase(Locale.ROOT),
                    passwordHash = hashedPassword,
                    birthday = birthday
                )
                val newUserId = repository.insertUser(newUser)

                // 3. Salvar informações adicionais do usuário no Firestore
                repository.saveUserToFirestore(newUser, firebaseUser.uid)

                // NOVO: Dispara a sincronização completa após o registro bem-sucedido

                //repository.performFullSync(newUserId, firebaseUser.uid)
                repository.performFullSync(1, firebaseUser.uid)

                _registerState.value = RegisterState.Success

            } catch (e: Exception) {
                _registerState.value =
                    RegisterState.Error("Ocorreu um erro inesperado: ${e.message}")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidDate(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.isLenient = false
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

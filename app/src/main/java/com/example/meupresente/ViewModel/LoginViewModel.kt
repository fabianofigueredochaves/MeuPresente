// corrigir linha 102

package com.example.meupresente.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meupresente.data.AppRepository
import com.example.meupresente.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import android.util.Patterns
import kotlinx.coroutines.tasks.await

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val repository: AppRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("E-mail e senha não podem estar em branco.")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                if (!isValidEmail(email)) {
                    _loginState.value = LoginState.Error("Formato de e-mail inválido.")
                    return@launch
                }

                val hashedPasswordInput = hashString(password)

                // 1. Tenta login localmente primeiro
                val localUser = repository.getUserByEmail(email)
                if (localUser != null && localUser.passwordHash == hashedPasswordInput) {
                    _loginState.value = LoginState.Success(localUser)

                    // Se online, tenta logar no Firebase em segundo plano e sincronizar
                    if (repository.isOnline()) {
                        val firebaseUser = repository.loginUserWithFirebase(email, password)
                        if (firebaseUser != null) {
                            // Certifica-se de que o usuário local tem o firebaseUid atualizado
                            if (localUser.firebaseUid == null || localUser.firebaseUid != firebaseUser.uid) {
                                repository.updateUser(localUser.copy(firebaseUid = firebaseUser.uid))
                            }
                            repository.performFullSync(localUser.id, firebaseUser.uid)
                        } else {
                            // Logged in locally, but Firebase login failed (e.g., password changed on Firebase).
                            // User can continue using offline, but will be notified for sync issues.
                            // For this example, we keep local login successful.
                        }
                    }
                    return@launch // Login local bem-sucedido
                }

                // 2. Se login local falhou, tenta login com Firebase (se estiver online)
                if (repository.isOnline()) {
                    val firebaseUser = repository.loginUserWithFirebase(email, password)
                    if (firebaseUser == null) {
                        _loginState.value = LoginState.Error("E-mail ou senha inválidos no Firebase.")
                        return@launch
                    }

                    // Login Firebase bem-sucedido
                    // Verifica se o usuário já existe localmente (pode ter registrado em outro dispositivo)
                    val user = repository.getUserByFirebaseUid(firebaseUser.uid)

                    if (user != null) {
                        // Usuário existe localmente, apenas atualiza e sincroniza
                        if (user.passwordHash != hashedPasswordInput) {
                            repository.updateUser(user.copy(passwordHash = hashedPasswordInput)) // Atualiza o hash local se mudou
                        }
                        _loginState.value = LoginState.Success(user)
                        repository.performFullSync(user.id, firebaseUser.uid)
                    } else {
                        // Usuário não existe localmente, é o primeiro login neste dispositivo.
                        // Busca dados do Firestore e insere localmente.
                        val firestoreUserDoc = repository.firestore.collection("users").document(firebaseUser.uid).get().await()
                        val name = firestoreUserDoc.getString("name")
                        val birthday = firestoreUserDoc.getString("birthday")

                        if (name != null && birthday != null) {
                            val newUser = User(
                                firebaseUid = firebaseUser.uid, // Associa ao Firebase UID
                                name = name,
                                email = email.lowercase(),
                                passwordHash = hashedPasswordInput,
                                birthday = birthday
                            )
                            val newUserId = repository.insertUser(newUser)
                          //  val finalUser = newUser.copy(id = newUserId)
                            val finalUser = newUser.copy(id = 10)
                            _loginState.value = LoginState.Success(finalUser)
                            repository.performFullSync(finalUser.id, firebaseUser.uid) // Inicia a sincronização
                        } else {
                            _loginState.value = LoginState.Error("Não foi possível recuperar dados do usuário do Firebase.")
                            repository.logoutFirebaseUser() // Desloga do Firebase se não conseguiu montar o usuário local
                        }
                    }
                } else {
                    // Offline e login local falhou
                    _loginState.value = LoginState.Error("Você está offline. E-mail ou senha inválidos para login local.")
                }
            } catch (e: Exception) {
                if (e.message?.contains("invalid-credential") == true || e.message?.contains("user-not-found") == true) {
                    _loginState.value = LoginState.Error("E-mail ou senha inválidos.")
                } else {
                    _loginState.value = LoginState.Error("Ocorreu um erro inesperado: ${e.message}")
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

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

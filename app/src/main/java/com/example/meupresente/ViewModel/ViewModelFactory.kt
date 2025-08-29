package com.example.meupresente.ViewModel

// ViewModelFactory.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.meupresente.data.AppRepository

import com.example.meupresente.ui.register.RegisterViewModel // Importe seu ViewModel

// Esta fábrica recebe o repositório como um parâmetro
class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {

    // A anotação é para suprimir um aviso de cast não verificado, que sabemos ser seguro aqui.
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verificamos qual ViewModel o sistema está pedindo para criar
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            // Se for o RegisterViewModel, nós o criamos e passamos o repositório
            return RegisterViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) { // <-- Adicionar este bloco
            return LoginViewModel(repository) as T
        }
        // Se for um ViewModel que não conhecemos, lançamos um erro.
        // Isso nos ajuda a não esquecer de adicionar novos ViewModels aqui.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
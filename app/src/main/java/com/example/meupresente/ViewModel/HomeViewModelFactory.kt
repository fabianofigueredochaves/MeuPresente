package com.example.meupresente.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.meupresente.data.AppRepository // Importe seu repositório

// A fábrica agora aceita o repositório E o ID do usuário
class HomeViewModelFactory(
    private val repository: AppRepository,
    private val userId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verificamos se a classe pedida é o HomeViewModel
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Se for, criamos a instância passando AMBOS os parâmetros
            return HomeViewModel(repository, userId) as T
        }
        // Se for um ViewModel que não conhecemos, lançamos um erro.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
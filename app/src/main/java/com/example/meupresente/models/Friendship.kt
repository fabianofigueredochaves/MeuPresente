package com.example.meupresente.models

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "friendships",
    primaryKeys = ["userId", "friendEmail"] // Chave primária composta
)
data class Friendship(
    val userId: Long, // ID do usuário que adicionou o amigo
    val friendEmail: String // Email do amigo adicionado
)
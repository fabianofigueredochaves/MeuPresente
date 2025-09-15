package com.example.meupresente.models

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "friendships",
    primaryKeys = ["userId", "friendEmail"] // Chave primária composta
)
data class Friendship(
    val userId: Long, // ID do usuário que adicionou o amigo
    val friendEmail: String, // Email do amigo adicionado
    val friendName: String? = null, // PODE SER NULO: Preenchido após sincronização
    val birthday: String? = null, // PODE SER NULO: Preenchido após sincronização
    val friendFirebaseUid: String? = null, // NOVO: UID do amigo no Firebase
    val syncStatus: SyncStatus = SyncStatus.SYNCED // NOVO: Status de sincronização
)
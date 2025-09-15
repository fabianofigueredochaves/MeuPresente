package com.example.meupresente.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// NOVO: Enum para o status de sincronização
enum class SyncStatus {
    PENDING_ADD,      // Criado localmente, precisa ser enviado ao Firebase
    PENDING_UPDATE,   // Modificado localmente, precisa ser atualizado no Firebase
    PENDING_DELETE,   // Excluído localmente, precisa ser excluído no Firebase
    SYNCED            // Sincronizado com o Firebase
}

@Entity(
    tableName = "gifts",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Gift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long, // Chave estrangeira para o usuário local
    val description: String,
    val isWanted: Boolean,
    val firestoreId: String? = null, // NOVO: ID do documento no Firestore
    val syncStatus: SyncStatus = SyncStatus.SYNCED // NOVO: Status de sincronização
)

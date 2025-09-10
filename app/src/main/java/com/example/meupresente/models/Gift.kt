package com.example.meupresente.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "gifts",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE // Se o usuário for deletado, seus presentes também serão
    )]
)
data class Gift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long, // Chave estrangeira para o usuário
    val description: String,
    val isWanted: Boolean, // true se ele quer ganhar, false se ele NÃO quer ganhar
)

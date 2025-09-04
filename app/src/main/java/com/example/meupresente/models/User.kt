package com.example.meupresente.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String, // Nunca armazene senhas em texto puro!
    val birthday: String // Pode usar String "dd/MM/yyyy" ou um Long para o timestamp

)
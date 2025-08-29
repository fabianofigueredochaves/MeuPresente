package com.example.meupresente.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.meupresente.models.User
import com.example.meupresente.models.Gift
import com.example.meupresente.models.Friendship
@Database(entities = [User::class, Gift::class, Friendship::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDAO(): AppDAO
}
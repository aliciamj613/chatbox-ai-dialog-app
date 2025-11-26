package com.example.chatbox.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.data.model.UserEntity

@Database(
    entities = [MessageEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
}

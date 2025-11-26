package com.example.chatbox.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatbox.data.model.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}

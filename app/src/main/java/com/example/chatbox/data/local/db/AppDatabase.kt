package com.example.chatbox.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.data.model.UserEntity

@Database(
    entities = [MessageEntity::class, UserEntity::class],
    version = 2,            // ⬅️ 把 1 改成 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chatbox.db"
                )
                    // ⬅️ 关键：如果 schema 变了，又没写 Migration，就直接删库重建
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

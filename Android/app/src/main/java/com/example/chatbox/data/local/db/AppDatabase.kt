package com.example.chatbox.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chatbox.data.model.ConversationEntity
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.data.model.UserEntity

/**
 * Room 数据库入口
 * 注意：version 每次你改 Entity 结构就 +1
 */
@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class,
        ConversationEntity::class
    ],
    version = 2,              // ✅ 原来如果是 1，现在改成 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chatbox.db"
                )
                    // ✅ 没有写 migration 时，版本变化就直接删库重建
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

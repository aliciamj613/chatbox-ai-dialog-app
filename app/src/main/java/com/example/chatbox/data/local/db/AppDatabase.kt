package com.example.chatbox.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.data.model.UserEntity
import com.example.chatbox.data.model.ConversationEntity

@Database(
    entities = [
        MessageEntity::class,
        UserEntity::class,
        ConversationEntity::class      // ✅ 新增会话表
    ],
    version = 4,                      // ✅ 版本号 +1（之前你是 2 或 3，这里统一用 4）
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao   // ✅ 新增 Dao 接口

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 兼容老代码：getInstance(context)
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chatbox.db"
                )
                    // 开发阶段：schema 变了直接删库重建，避免 Room 崩溃
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 新代码里也可以用这个别名
         */
        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}

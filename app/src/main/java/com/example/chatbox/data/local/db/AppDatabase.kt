package com.example.chatbox.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.data.model.UserEntity

@Database(
    entities = [MessageEntity::class, UserEntity::class],
    version = 3,          // 记得比之前大就行（你之前 1/2 都没事）
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 旧代码里用的名字：getInstance(context)
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chatbox.db"
                )
                    // 开发阶段：表结构变了就直接删库重建，避免 Room 崩
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 新代码里用的别名：getDatabase(context)
         */
        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}

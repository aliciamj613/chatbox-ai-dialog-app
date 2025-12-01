// app/src/main/java/com/example/chatbox/data/local/prefs/UserPreferences.kt
package com.example.chatbox.data.local.prefs

import android.content.Context

class UserPreferences(context: Context) {

    private val sp = context.getSharedPreferences("chatbox_prefs", Context.MODE_PRIVATE)

    fun saveLastUserId(userId: Long) {
        sp.edit()
            .putLong("last_user_id", userId)
            .apply()
    }

    fun getLastUserId(): Long? {
        val id = sp.getLong("last_user_id", -1L)
        return if (id == -1L) null else id
    }
}

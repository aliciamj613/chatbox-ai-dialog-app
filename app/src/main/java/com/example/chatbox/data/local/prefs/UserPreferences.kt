package com.example.chatbox.data.local.prefs

import android.content.Context

class UserPreferences(context: Context) {

    private val sp = context.getSharedPreferences("chatbox_prefs", Context.MODE_PRIVATE)

    fun saveLastUserId(userId: String) {
        sp.edit()
            .putString("last_user_id", userId)
            .apply()
    }

    fun getLastUserId(): String? =
        sp.getString("last_user_id", null)
}

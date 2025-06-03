package com.beinny.teamboard.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.beinny.teamboard.utils.Constants

class SharedPrefsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.TEAMBOARD_PREFERENCES, Context.MODE_PRIVATE
    )

    fun isFcmTokenUpdated(): Boolean {
        return prefs.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
    }

    fun setFcmTokenUpdated(updated: Boolean) {
        prefs.edit().putBoolean(Constants.FCM_TOKEN_UPDATED, updated).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
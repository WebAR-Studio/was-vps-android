package com.arstudio.wasvps_sdk.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.arstudio.wasvps_android_prototype.util.UUIDv7
import java.util.*

internal class PrefsRepository(private val context: Context) : IPrefsRepository {

    private companion object {
        const val PREFS_NAME = "vps_sdk_prefs"
        const val USER_ID = "user_id"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun getUserId(): String =
        sharedPreferences.getString(USER_ID, null) ?: generateUserId()

    private fun generateUserId(): String =
        UUIDv7.randomUUID()
            .toString()
            .also {
                sharedPreferences.edit()
                    .putString(USER_ID, it)
                    .apply()
            }

}
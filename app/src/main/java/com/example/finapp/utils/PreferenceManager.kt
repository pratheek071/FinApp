package com.example.finapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.finapp.data.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    
    var userId: String?
        get() = prefs.getString(Constants.PREF_USER_ID, null)
        set(value) = prefs.edit().putString(Constants.PREF_USER_ID, value).apply()
    
    var userRole: UserRole?
        get() = prefs.getString(Constants.PREF_USER_ROLE, null)?.let {
            try { UserRole.valueOf(it) } catch (e: Exception) { null }
        }
        set(value) = prefs.edit().putString(Constants.PREF_USER_ROLE, value?.name).apply()
    
    var userName: String?
        get() = prefs.getString(Constants.PREF_USER_NAME, null)
        set(value) = prefs.edit().putString(Constants.PREF_USER_NAME, value).apply()
    
    var userPhone: String?
        get() = prefs.getString(Constants.PREF_USER_PHONE, null)
        set(value) = prefs.edit().putString(Constants.PREF_USER_PHONE, value).apply()
    
    fun isLoggedIn(): Boolean {
        return userId != null && userRole != null
    }
    
    fun clear() {
        prefs.edit().clear().apply()
    }
}


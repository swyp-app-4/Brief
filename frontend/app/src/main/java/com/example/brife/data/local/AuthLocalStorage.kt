package com.example.brife.data.local

import android.content.Context

class AuthLocalStorage(context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun saveRefreshToken(token: String) {
        prefs.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun saveLoginMethod(method: String) {
        prefs.edit().putString("login_method", method).apply()
    }

    fun getLoginMethod(): String? = prefs.getString("login_method", null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}

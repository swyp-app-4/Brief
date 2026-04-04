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



    //  약관 동의 상태를 저장하고 관리하는 메서드
    fun hasAgreedTerms(): Boolean = prefs.getBoolean("has_agreed_terms", false)

    fun saveTermsAgreement(agreed: Boolean) {
        prefs.edit().putBoolean("has_agreed_terms", agreed).apply()
    }

    // 로그아웃 시 사용 (인증 정보만 삭제)
    fun clearAuthOnly() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("login_method")
            .remove("has_agreed_terms")
            .apply()
    }



    fun clear() {
        prefs.edit().clear().apply()
    }
}

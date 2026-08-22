package com.swyp.brife.data.local

import android.content.Context
import com.swyp.brife.ui.theme.ThemeMode

class ThemeModeLocalStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(): ThemeMode = prefs.getString(KEY_THEME_MODE, null)
        ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
        ?: ThemeMode.SYSTEM

    fun saveThemeMode(themeMode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "theme_prefs"
        const val KEY_THEME_MODE = "theme_mode"
    }
}

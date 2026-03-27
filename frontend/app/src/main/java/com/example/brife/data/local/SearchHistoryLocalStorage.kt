package com.example.brife.data.local

import android.content.Context

class SearchHistoryLocalStorage(context: Context) {

    private val prefs = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    private val key = "recent_queries"
    private val maxSize = 10

    fun getRecentQueries(): List<String> {
        val value = prefs.getString(key, "") ?: ""
        if (value.isBlank()) return emptyList()
        return value.split("||").filter { it.isNotBlank() }
    }

    fun addQuery(query: String) {
        val current = getRecentQueries().toMutableList()
        current.remove(query)        // 중복 제거
        current.add(0, query)        // 최신 항목을 맨 앞에
        val trimmed = current.take(maxSize)
        prefs.edit().putString(key, trimmed.joinToString("||")).apply()
    }

    fun removeQuery(query: String) {
        val current = getRecentQueries().toMutableList()
        current.remove(query)
        prefs.edit().putString(key, current.joinToString("||")).apply()
    }

    fun clearAll() {
        prefs.edit().remove(key).apply()
    }
}

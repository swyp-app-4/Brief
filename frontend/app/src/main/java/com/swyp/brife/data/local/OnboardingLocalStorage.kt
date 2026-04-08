package com.swyp.brife.data.local

import android.content.Context

class OnboardingLocalStorage(context: Context) {

    private val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    fun saveSelectedCategoryIds(categoryIds: List<Long>) {
        prefs.edit()
            .putString("selected_category_ids", categoryIds.joinToString(","))
            .apply()
    }

    fun getSelectedCategoryIds(): List<Long> {
        val value = prefs.getString("selected_category_ids", "") ?: ""
        if (value.isBlank()) return emptyList()
        return value.split(",").mapNotNull { it.toLongOrNull() }
    }

    fun saveSelectedSubCategoryIds(subCategoryIds: List<Long>) {
        prefs.edit()
            .putString("selected_sub_category_ids", subCategoryIds.joinToString(","))
            .apply()
    }

    fun getSelectedSubCategoryIds(): List<Long> {
        val value = prefs.getString("selected_sub_category_ids", "") ?: ""
        if (value.isBlank()) return emptyList()
        return value.split(",").mapNotNull { it.toLongOrNull() }
    }

    fun saveOnboardingCompleted(isCompleted: Boolean) {
        prefs.edit()
            .putBoolean("has_completed_onboarding", isCompleted)
            .apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean("has_completed_onboarding", false)
    }

    fun hasSavedInterests(): Boolean {
        return getSelectedCategoryIds().isNotEmpty() ||
            getSelectedSubCategoryIds().isNotEmpty() ||
            hasCompletedOnboarding()
    }

    fun clearOnboarding() {
        prefs.edit().clear().apply()
    }
}

package com.swyp.brife.feature.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast

object WidgetPinHelper {

    fun requestPinWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(context, BrifeWidgetReceiver::class.java)
            registerCurrentThemePreview(context, appWidgetManager, provider)
            appWidgetManager.requestPinAppWidget(provider, null, null)
        } else {
            Toast.makeText(
                context,
                "현재 런처에서는 위젯 자동 설치를 지원하지 않습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun registerCurrentThemePreview(
        context: Context,
        appWidgetManager: AppWidgetManager,
        provider: ComponentName
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

        val isDarkTheme = WidgetThemeHelper.isDarkTheme(context)
        val previewPreferences = context.getSharedPreferences(
            PREVIEW_PREFS_NAME,
            Context.MODE_PRIVATE
        )
        if (previewPreferences.contains(KEY_PREVIEW_IS_DARK) &&
            previewPreferences.getBoolean(KEY_PREVIEW_IS_DARK, false) == isDarkTheme &&
            previewPreferences.getInt(KEY_PREVIEW_STYLE_VERSION, 0) == PREVIEW_STYLE_VERSION
        ) {
            return
        }

        val registered = appWidgetManager.setWidgetPreview(
            provider,
            AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
            WidgetThemeHelper.createPreviewRemoteViews(context, isDarkTheme)
        )
        if (registered) {
            previewPreferences.edit()
                .putBoolean(KEY_PREVIEW_IS_DARK, isDarkTheme)
                .putInt(KEY_PREVIEW_STYLE_VERSION, PREVIEW_STYLE_VERSION)
                .apply()
        }
    }

    private const val PREVIEW_PREFS_NAME = "widget_preview_prefs"
    private const val KEY_PREVIEW_IS_DARK = "preview_is_dark"
    private const val KEY_PREVIEW_STYLE_VERSION = "preview_style_version"
    private const val PREVIEW_STYLE_VERSION = 3
}

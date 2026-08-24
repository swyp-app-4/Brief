package com.swyp.brife.feature.widget

import android.content.Context
import android.content.res.Configuration
import android.widget.RemoteViews
import com.swyp.brife.R
import com.swyp.brife.data.local.ThemeModeLocalStorage
import com.swyp.brife.ui.theme.ThemeMode

internal object WidgetThemeHelper {

    fun isDarkTheme(context: Context): Boolean = when (ThemeModeLocalStorage(context).getThemeMode()) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM ->
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
    }

    fun applyContainerTheme(context: Context, views: RemoteViews, isDarkTheme: Boolean) {
        views.setInt(
            R.id.widget_root,
            "setBackgroundColor",
            context.getColor(if (isDarkTheme) R.color.widget_background_dark else R.color.widget_background_light)
        )
        val previousButtonBackground = if (isDarkTheme) {
            R.drawable.widget_nav_button_bg_dark
        } else {
            R.drawable.widget_nav_button_bg_light
        }
        val nextButtonBackground = if (isDarkTheme) {
            R.drawable.widget_nav_button_next_bg_dark
        } else {
            R.drawable.widget_nav_button_next_bg_light
        }
        views.setInt(R.id.widget_btn_prev, "setBackgroundResource", previousButtonBackground)
        views.setInt(R.id.widget_btn_next, "setBackgroundResource", nextButtonBackground)
    }

    fun applyItemTheme(context: Context, views: RemoteViews, isDarkTheme: Boolean) {
        views.setTextColor(
            R.id.widget_tv_title,
            context.getColor(if (isDarkTheme) R.color.widget_title_dark else R.color.widget_title_light)
        )
        views.setInt(
            R.id.widget_card_body,
            "setBackgroundResource",
            if (isDarkTheme) R.drawable.widget_card_bg_dark else R.drawable.widget_card_bg_light
        )
        val summaryColor = context.getColor(
            if (isDarkTheme) R.color.widget_summary_dark else R.color.widget_summary_light
        )
        views.setTextColor(R.id.widget_tv_summary_1, summaryColor)
        views.setTextColor(R.id.widget_tv_summary_2, summaryColor)
        views.setTextColor(R.id.widget_tv_summary_3, summaryColor)
        views.setTextColor(R.id.widget_tv_summary_4, summaryColor)
    }

    fun createPreviewRemoteViews(context: Context, isDarkTheme: Boolean): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_preview_layout).also { views ->
            views.setInt(
                R.id.widget_preview_root,
                "setBackgroundColor",
                context.getColor(
                    if (isDarkTheme) R.color.widget_background_dark else R.color.widget_background_light
                )
            )
            views.setTextColor(
                R.id.widget_preview_title,
                context.getColor(if (isDarkTheme) R.color.widget_title_dark else R.color.widget_title_light)
            )
            views.setInt(
                R.id.widget_preview_card_body,
                "setBackgroundResource",
                if (isDarkTheme) R.drawable.widget_card_bg_dark else R.drawable.widget_card_bg_light
            )
            val summaryColor = context.getColor(
                if (isDarkTheme) R.color.widget_summary_dark else R.color.widget_summary_light
            )
            views.setTextColor(R.id.widget_preview_summary_1, summaryColor)
            views.setTextColor(R.id.widget_preview_summary_2, summaryColor)
            views.setTextColor(R.id.widget_preview_summary_3, summaryColor)
            views.setTextColor(R.id.widget_preview_summary_4, summaryColor)
            val previousButtonBackground = if (isDarkTheme) {
                R.drawable.widget_nav_button_bg_dark
            } else {
                R.drawable.widget_nav_button_bg_light
            }
            val nextButtonBackground = if (isDarkTheme) {
                R.drawable.widget_nav_button_next_bg_dark
            } else {
                R.drawable.widget_nav_button_next_bg_light
            }
            views.setInt(
                R.id.widget_preview_btn_prev,
                "setBackgroundResource",
                previousButtonBackground
            )
            views.setInt(
                R.id.widget_preview_btn_next,
                "setBackgroundResource",
                nextButtonBackground
            )
        }
}

package com.swyp.brife.feature.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast

object WidgetPinHelper {

    fun requestPinWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(context, BrifeWidgetReceiver::class.java)
            appWidgetManager.requestPinAppWidget(provider, null, null)
        } else {
            Toast.makeText(
                context,
                "현재 런처에서는 위젯 자동 설치를 지원하지 않습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

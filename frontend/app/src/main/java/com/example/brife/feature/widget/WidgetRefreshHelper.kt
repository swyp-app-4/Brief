package com.example.brife.feature.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.brife.R

object WidgetRefreshHelper {

    fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, BrifeWidgetReceiver::class.java)
        )

        if (ids.isEmpty()) return

        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_flipper)
        context.sendBroadcast(
            Intent(context, BrifeWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
        )
    }
}

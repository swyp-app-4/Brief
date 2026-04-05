package com.example.brife.feature.widget

import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService

class BrifeWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        Log.d("BrifeWidgetService", "onGetViewFactory 호출")

        return BrifeWidgetFactory(applicationContext)
    }
}

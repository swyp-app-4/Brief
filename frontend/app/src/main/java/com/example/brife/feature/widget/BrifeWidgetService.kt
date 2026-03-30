package com.example.brife.feature.widget

import android.content.Intent
import android.widget.RemoteViewsService

class BrifeWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return BrifeWidgetFactory(applicationContext)
    }
}

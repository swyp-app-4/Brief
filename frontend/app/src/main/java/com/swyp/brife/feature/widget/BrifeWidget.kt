package com.swyp.brife.feature.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.swyp.brife.R

class BrifeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BrifeWidgetContent()
        }
    }
}

@Composable
private fun BrifeWidgetContent() {
    val context = LocalContext.current

    val blackColor = androidx.glance.color.ColorProvider(day = Color.Black, night = Color.Black)
    val whiteColor = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White)

    // ── StackView RemoteViews 구성 ──────────────────────────────────────────
    val stackRootViews = RemoteViews(context.packageName, R.layout.widget_stack_root)
    stackRootViews.setRemoteAdapter(
        R.id.widget_stack_view,
        Intent(context, BrifeWidgetService::class.java)
    )

    // ── PendingIntentTemplate 설정 ───────────────────────────────────────────
    // BrifeWidgetFactory.getViewAt() 에서 setOnClickFillInIntent() 로 설정된
    // fill-in Intent 와 이 template 이 합쳐져 WidgetActionReceiver 가 호출됨
    val templateIntent = Intent(context, WidgetActionReceiver::class.java)
    val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    val pendingTemplate = PendingIntent.getBroadcast(context, 0, templateIntent, pendingFlags)
    stackRootViews.setPendingIntentTemplate(R.id.widget_stack_view, pendingTemplate)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(whiteColor)
            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp)
    ) {
        // ── 상단 Row: 로고 + "Brife" ──────────────────────────────────────────
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.brife_logo),
                contentDescription = "Brife 로고",
                modifier = GlanceModifier.size(20.dp)
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "Brief",
                style = TextStyle(
                    color = blackColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // ── 스와이프 카드 영역 (RemoteViews StackView) ────────────────────────
        AndroidRemoteViews(
            remoteViews = stackRootViews,
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
        )
    }
}

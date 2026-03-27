package com.example.brife.feature.widget

import android.content.Context
import android.content.Intent
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
import androidx.glance.unit.ColorProvider
import com.example.brife.R

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

    // StackView RemoteViews 구성
    val stackRootViews = RemoteViews(context.packageName, R.layout.widget_stack_root)
    stackRootViews.setRemoteAdapter(
        R.id.widget_stack_view,
        Intent(context, BrifeWidgetService::class.java)
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(whiteColor)
            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp)
    ) {
        // ── 상단 Row: 로고 + "Brife" + 북마크 ────────────────────────────────
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
                text = "Brife",
                style = TextStyle(
                    color = blackColor,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(R.drawable.ic_longform_bookmark_inactive),
                contentDescription = "즐겨찾기",
                modifier = GlanceModifier.size(20.dp)
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // ── 스와이프 카드 영역 (RemoteViews StackView) ────────────────────────
        // 사용자가 좌우로 스와이프하면 카드가 전환됩니다.
        // 각 카드 하단의 인디케이터는 HomeScreen과 동일한 색상을 사용합니다.
        // active=White(#FFFFFF), inactive=White 50%(#80FFFFFF)
        AndroidRemoteViews(
            remoteViews = stackRootViews,
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
        )
    }
}

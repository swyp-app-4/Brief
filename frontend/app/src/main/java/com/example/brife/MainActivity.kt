package com.example.brife

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.brife.navigation.AppNavGraph
import com.example.brife.ui.theme.BrifeTheme

class MainActivity : ComponentActivity() {

    // setContent 바깥 Activity 수준 state → onNewIntent에서도 업데이트 가능
    // 위젯 클릭 등으로 앱이 이미 실행 중일 때 onNewIntent 가 호출되어도 Compose 가 재구성됨
    private var deepLinkNewsId by mutableStateOf<Long?>(null)
    private var deepLinkOpenBookmark by mutableStateOf(false)

    private fun extractBookmarkFromIntent(intent: Intent?): Boolean =
        intent?.getBooleanExtra("open_bookmark", false) ?: false

    private fun extractNewsIdFromIntent(intent: Intent?): Long? {
        val data: Uri = intent?.data ?: return null

        // 예: https://brife.app/news/1935
        val segments = data.pathSegments
        return if (
            data.scheme == "https" &&
            data.host == "brife.app" &&
            segments.size >= 2 &&
            segments[0] == "news"
        ) {
            segments[1].toLongOrNull()
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        deepLinkNewsId = extractNewsIdFromIntent(intent)
        deepLinkOpenBookmark = extractBookmarkFromIntent(intent)

        setContent {
            BrifeTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                        )
                ) {
                    AppNavGraph(
                        initialNewsId = deepLinkNewsId,
                        initialOpenBookmark = deepLinkOpenBookmark
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkNewsId = extractNewsIdFromIntent(intent)
        deepLinkOpenBookmark = extractBookmarkFromIntent(intent)
    }
}

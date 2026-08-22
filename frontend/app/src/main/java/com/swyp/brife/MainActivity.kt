package com.swyp.brife

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
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
import com.swyp.brife.navigation.AppNavGraph
import com.swyp.brife.feature.notification.BriefNotificationHelper
import com.swyp.brife.ui.theme.BrifeTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // 알림 권한 거부는 앱 사용 흐름을 막지 않습니다.
    }

    // newsId + version 쌍으로 관리 → 동일 newsId를 다시 탭해도 version이 증가해 LaunchedEffect 재실행
    private var deepLinkNewsId by mutableStateOf<Long?>(null)
    private var deepLinkOpenBookmark by mutableStateOf(false)
    private var deepLinkVersion by mutableStateOf(0)
    private var notificationPrimaryNewsId by mutableStateOf<Long?>(null)
    private var notificationAnchorVersion by mutableStateOf(0)

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

    private fun extractPrimaryNewsIdFromIntent(intent: Intent?): Long? {
        val rawValue = intent?.extras?.get(BriefNotificationHelper.EXTRA_PRIMARY_NEWS_ID)
        return when (rawValue) {
            is Long -> rawValue
            is Int -> rawValue.toLong()
            is String -> rawValue.toLongOrNull()
            else -> null
        }?.takeIf { it > 0L }
    }

    private fun consumeNotificationAnchor() {
        notificationPrimaryNewsId = null
        intent?.removeExtra(BriefNotificationHelper.EXTRA_PRIMARY_NEWS_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        deepLinkNewsId = extractNewsIdFromIntent(intent)
        deepLinkOpenBookmark = extractBookmarkFromIntent(intent)
        notificationPrimaryNewsId = extractPrimaryNewsIdFromIntent(intent)

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
                        initialOpenBookmark = deepLinkOpenBookmark,
                        deepLinkVersion = deepLinkVersion,
                        notificationPrimaryNewsId = notificationPrimaryNewsId,
                        notificationAnchorVersion = notificationAnchorVersion,
                        onNotificationAnchorConsumed = ::consumeNotificationAnchor
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
        notificationPrimaryNewsId = extractPrimaryNewsIdFromIntent(intent)
        if (notificationPrimaryNewsId != null) {
            notificationAnchorVersion++
        }
        // 동일 newsId라도 매번 버전 증가 → LaunchedEffect 재실행 보장
        deepLinkVersion++
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val isGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

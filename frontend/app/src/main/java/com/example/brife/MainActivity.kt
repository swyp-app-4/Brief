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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.brife.navigation.AppNavGraph
import com.example.brife.ui.theme.BrifeTheme

class MainActivity : ComponentActivity() {


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

        val initialNewsId = extractNewsIdFromIntent(intent)


        setContent {
            BrifeTheme {
                val deepLinkNewsIdState = remember { mutableStateOf(initialNewsId) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                        )
                ) {
                    AppNavGraph(
                        initialNewsId = deepLinkNewsIdState.value
                    )
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
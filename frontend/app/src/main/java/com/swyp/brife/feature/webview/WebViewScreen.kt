package com.swyp.brife.feature.webview

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.swyp.brife.ui.component.AppTopBar2

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    title: String,
    url: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            AppTopBar2(
                title = title,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            Log.d("WebViewScreen", "onPageStarted: $url")
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            Log.d("WebViewScreen", "onPageFinished: $url")
                        }
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            Log.e("WebViewScreen", "onReceivedError: ${error?.description} / url: ${request?.url}")
                        }
                    }
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true       // Notion SPA의 localStorage 접근에 필요
                        useWideViewPort = true         // 페이지 viewport 메타태그 적용
                        loadWithOverviewMode = true    // 화면 너비에 맞게 축소 허용
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    loadUrl(url)
                }
            },
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

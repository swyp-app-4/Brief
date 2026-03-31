package com.example.brife.feature.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import java.io.File
import java.io.FileOutputStream

/**
 * Window 영역을 Bitmap으로 캡처한다. (HomeScreen 카드 공유 등 viewport 내 콘텐츠용)
 *
 * - API 26+: PixelCopy 사용 (하드웨어 가속된 Compose View에서도 정상 동작)
 * - 미만: Canvas 소프트웨어 렌더링 폴백
 */
fun captureWindowBitmap(
    context: Context,
    view: View,
    srcRect: Rect? = null,
    onCaptured: (Bitmap) -> Unit
) {
    val activity = context as? Activity ?: run {
        onCaptured(softwareDraw(view, srcRect))
        return
    }
    val width = srcRect?.width() ?: view.width
    val height = srcRect?.height() ?: view.height
    if (width <= 0 || height <= 0) return

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    PixelCopy.request(
        activity.window,
        srcRect,
        bitmap,
        { result ->
            if (result == PixelCopy.SUCCESS) onCaptured(bitmap)
        },
        Handler(Looper.getMainLooper())
    )
}

private fun softwareDraw(view: View, cropRect: Rect?): Bitmap {
    val fullBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(fullBitmap)
    view.draw(canvas)
    return if (cropRect != null) {
        Bitmap.createBitmap(
            fullBitmap,
            cropRect.left.coerceAtLeast(0),
            cropRect.top.coerceAtLeast(0),
            cropRect.width().coerceAtMost(view.width - cropRect.left.coerceAtLeast(0)),
            cropRect.height().coerceAtMost(view.height - cropRect.top.coerceAtLeast(0))
        )
    } else {
        fullBitmap
    }
}

/**
 * 주어진 Composable을 화면 밖에서 전체 높이로 렌더링한 뒤 Bitmap으로 캡처한다.
 *
 * PixelCopy는 현재 GPU 프레임버퍼(viewport)만 복사하므로 스크롤 전체 캡처 불가.
 * 이 함수는 ComposeView를 decor view에 화면 밖으로 추가해 실제 Compose 레이아웃을
 * 전체 높이로 수행한 뒤, 소프트웨어 Canvas로 전체 Bitmap을 생성한다.
 *
 * @param context Activity Context
 * @param onCaptured 캡처 완료 시 Bitmap 전달 (메인 스레드)
 * @param content 캡처할 Composable — 스크롤 없이 자연 높이로 렌더링되어야 함
 */
fun captureComposableContent(
    context: Context,
    onCaptured: (Bitmap) -> Unit,
    content: @Composable () -> Unit
) {
    val activity = context as? Activity ?: return
    val decorView = activity.window.decorView as? ViewGroup ?: return
    val screenWidth = decorView.width.takeIf { it > 0 } ?: return

    val composeView = ComposeView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            screenWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 화면 오른쪽 밖으로 이동 — 사용자에게 보이지 않음
        translationX = screenWidth.toFloat()
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent { content() }
    }

    decorView.addView(composeView)

    // doOnLayout: measure/layout 완료 후 호출
    // postDelayed: Compose 첫 프레임 렌더링 완료까지 대기
    composeView.doOnLayout {
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (!composeView.isAttachedToWindow) return@postDelayed

                val height = composeView.height.coerceAtMost(12_000) // OOM 방지
                if (height <= 0) {
                    decorView.removeView(composeView)
                    return@postDelayed
                }

                // hardware-accelerated 뷰는 drawToBitmap 불가 → software 강제
                composeView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                val bitmap = Bitmap.createBitmap(screenWidth, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(android.graphics.Color.WHITE)
                composeView.draw(canvas)

                decorView.removeView(composeView)
                onCaptured(bitmap)
            } catch (e: Exception) {
                if (composeView.isAttachedToWindow) decorView.removeView(composeView)
            }
        }, 200L)
    }
}

/**
 * Bitmap을 cache 디렉토리에 저장하고 시스템 공유 시트로 이미지를 공유한다.
 */
fun shareImageBitmap(context: Context, bitmap: Bitmap, extraText: String = "") {
    val cachePath = File(context.cacheDir, "shared_images").also { it.mkdirs() }
    val imageFile = File(cachePath, "share_${System.currentTimeMillis()}.png")
    FileOutputStream(imageFile).use { fos ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        if (extraText.isNotBlank()) putExtra(Intent.EXTRA_TEXT, extraText)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

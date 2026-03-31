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
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Window 영역을 Bitmap으로 캡처한다.
 *
 * @param srcRect null이면 전체 window 캡처, non-null이면 해당 window 좌표 영역만 캡처
 *                (LayoutCoordinates.boundsInWindow() 값을 Rect로 변환해서 전달)
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
 * Bitmap을 cache 디렉토리에 저장하고 시스템 공유 시트로 이미지를 공유한다.
 * FLAG_GRANT_READ_URI_PERMISSION 포함.
 *
 * @param extraText 함께 공유할 텍스트 (선택). 비어 있으면 포함하지 않음
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

package com.swyp.brife.feature.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import com.swyp.brife.R
import java.io.File
import java.io.FileOutputStream

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
        translationX = screenWidth.toFloat()
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setContent { content() }
    }

    decorView.addView(composeView)

    val handler = Handler(Looper.getMainLooper())
    var layoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (composeView.height > 0) {
            composeView.viewTreeObserver
                .takeIf { it.isAlive }
                ?.removeOnGlobalLayoutListener(layoutListener)

            handler.postDelayed({
                try {
                    if (!composeView.isAttachedToWindow) return@postDelayed

                    composeView.measure(
                        View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    val fullHeight = composeView.measuredHeight.coerceIn(1, 12_000)
                    composeView.layout(
                        composeView.left,
                        composeView.top,
                        composeView.left + screenWidth,
                        composeView.top + fullHeight
                    )

                    val bitmap = Bitmap.createBitmap(screenWidth, fullHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    composeView.draw(canvas)

                    decorView.removeView(composeView)
                    onCaptured(bitmap)
                } catch (_: Exception) {
                    if (composeView.isAttachedToWindow) decorView.removeView(composeView)
                }
            }, 300L)
        }
    }
    composeView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
}

fun shareImageBitmap(context: Context, bitmap: Bitmap, extraText: String = "") {
    val sharedBitmap = bitmap.withShareWatermark(context)
    val cachePath = File(context.cacheDir, "shared_images").also { it.mkdirs() }
    val imageFile = File(cachePath, "share_${System.currentTimeMillis()}.png")
    FileOutputStream(imageFile).use { fos ->
        sharedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
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

private fun Bitmap.withShareWatermark(context: Context): Bitmap {
    val baseBitmap = if (config == Bitmap.Config.ARGB_8888) this else copy(Bitmap.Config.ARGB_8888, false)
    val resultBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val watermarkBitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.img_brief_watermark
    ) ?: return resultBitmap

    val marginPx = (14f * context.resources.displayMetrics.density).toInt()
    val left = (resultBitmap.width - watermarkBitmap.width - marginPx).coerceAtLeast(0)
    val top = (resultBitmap.height - watermarkBitmap.height - marginPx).coerceAtLeast(0)

    val canvas = Canvas(resultBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(watermarkBitmap, left.toFloat(), top.toFloat(), paint)
    watermarkBitmap.recycle()
    return resultBitmap
}

package com.swyp.brife.feature.home

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Dp
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

data class ShareArtifact(val uri: Uri)

fun captureTransparentComposableContent(
    context: Context,
    width: Dp,
    minHeight: Dp,
    onResult: (Result<Bitmap>) -> Unit,
    content: @Composable () -> Unit
) {
    val activity = context as? Activity
        ?: return onResult(Result.failure(IllegalStateException("Activity context is required")))
    val decorView = activity.window.decorView as? ViewGroup
        ?: return onResult(Result.failure(IllegalStateException("Decor view is unavailable")))
    val density = context.resources.displayMetrics.density
    val widthPx = (width.value * density).roundToInt().coerceAtLeast(1)
    val minHeightPx = (minHeight.value * density).roundToInt().coerceAtLeast(1)

    val composeView = ComposeView(context).apply {
        layoutParams = FrameLayout.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
        translationX = decorView.width.toFloat()
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setContent { content() }
    }

    decorView.addView(composeView)
    val handler = Handler(Looper.getMainLooper())
    handler.post {
        try {
            composeView.measure(
                View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val heightPx = composeView.measuredHeight.coerceAtLeast(minHeightPx)
            composeView.layout(0, 0, widthPx, heightPx)

            handler.postDelayed({
                try {
                    if (!composeView.isAttachedToWindow) {
                        onResult(Result.failure(IllegalStateException("Share card view was detached")))
                        return@postDelayed
                    }

                    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    composeView.draw(canvas)
                    decorView.removeView(composeView)
                    onResult(Result.success(bitmap))
                } catch (exception: Exception) {
                    if (composeView.isAttachedToWindow) decorView.removeView(composeView)
                    onResult(Result.failure(exception))
                }
            }, 300L)
        } catch (exception: Exception) {
            if (composeView.isAttachedToWindow) decorView.removeView(composeView)
            onResult(Result.failure(exception))
        }
    }
}

fun createShareArtifact(context: Context, bitmap: Bitmap): Result<ShareArtifact> = runCatching {
    val cachePath = File(context.cacheDir, "shared_images").also { it.mkdirs() }
    val imageFile = File(cachePath, "share_card_${System.currentTimeMillis()}.png")
    FileOutputStream(imageFile).use { output ->
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
            "Failed to encode share card"
        }
    }

    ShareArtifact(
        uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    )
}

fun shareViaSystem(context: Context, artifact: ShareArtifact, title: String): Result<Unit> =
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, artifact.uri)
            if (title.isNotBlank()) putExtra(Intent.EXTRA_TEXT, title)
            clipData = ClipData.newRawUri("news_share_card", artifact.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

fun shareViaInstagramStory(context: Context, artifact: ShareArtifact): Result<Unit> = runCatching {
    val readPermission = Intent.FLAG_GRANT_READ_URI_PERMISSION

    context.grantUriPermission("com.instagram.android", artifact.uri, readPermission)

    val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setPackage("com.instagram.android")
        type = "image/png"
        putExtra("interactive_asset_uri", artifact.uri)
        putExtra("top_background_color", "#F7F9FD")
        putExtra("bottom_background_color", "#F7F9FD")
        clipData = ClipData.newRawUri("instagram_story_sticker", artifact.uri)
        addFlags(readPermission)
        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}

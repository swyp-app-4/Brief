package com.swyp.brife.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrifeColorScheme = lightColorScheme(
    primary = PrimaryNormal,
    onPrimary = Color.White,

    secondary = SecondaryNormal,
    onSecondary = Color.White,

    background = BgDefault,
    onBackground = TextTitle,

    surface = BgDefault,
    onSurface = TextTitle,

    surfaceVariant = BgSub,
    onSurfaceVariant = TextBody,

    outline = BorderStrong,
    error = Negative,
    onError = Color.White
)

@Composable
fun BrifeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrifeColorScheme,
        typography = Typography,
        content = content
    )
}
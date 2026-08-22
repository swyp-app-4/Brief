package com.swyp.brife.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Immutable
data class BrifeColors(
    val textTitle: Color,
    val textSubtitle: Color,
    val textBody: Color,
    val textCaption: Color,
    val backgroundDefault: Color,
    val backgroundSub: Color,
    val componentDefault: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val ctaActive: Color,
    val ctaDisabled: Color,
    val dimmer: Color,
    val homeAccent: Color
)

private val LightBrifeColors = BrifeColors(
    textTitle = TextTitle,
    textSubtitle = TextSubtitle,
    textBody = TextBody,
    textCaption = TextCaption,
    backgroundDefault = BgDefault,
    backgroundSub = BgSub,
    componentDefault = ComponentDefault,
    borderDefault = BorderDefault,
    borderStrong = BorderStrong,
    ctaActive = CtaActive,
    ctaDisabled = CtaDisabled,
    dimmer = Dimmer,
    homeAccent = Blue900
)

private val DarkBrifeColors = BrifeColors(
    textTitle = DarkTextTitle,
    textSubtitle = DarkTextSubtitle,
    textBody = TextBody,
    textCaption = TextCaption,
    backgroundDefault = DarkBackground,
    backgroundSub = DarkGray200,
    componentDefault = DarkComponentDefault,
    borderDefault = DarkBorderDefault,
    borderStrong = DarkBorderStrong,
    ctaActive = CtaActive,
    ctaDisabled = DarkCtaDisabled,
    dimmer = Dimmer,
    homeAccent = Blue900
)

val LocalBrifeColors = staticCompositionLocalOf { LightBrifeColors }
val MaterialTheme.brifeColors: BrifeColors
    @Composable get() = LocalBrifeColors.current

private val LightColorScheme = lightColorScheme(
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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNormal,
    onPrimary = Color.White,
    secondary = DarkGray900,
    onSecondary = DarkTextTitle,
    background = DarkBackground,
    onBackground = DarkTextTitle,
    surface = DarkBackground,
    onSurface = DarkTextTitle,
    surfaceVariant = DarkComponentDefault,
    onSurfaceVariant = TextBody,
    outline = DarkBorderStrong,
    error = Negative,
    onError = Color.White,
    scrim = Dimmer
)

@Composable
fun BrifeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkBrifeColors else LightBrifeColors
    CompositionLocalProvider(LocalBrifeColors provides colors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

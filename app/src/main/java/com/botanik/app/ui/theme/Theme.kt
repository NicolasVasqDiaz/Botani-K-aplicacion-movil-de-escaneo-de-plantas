package com.botanik.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BotaniKColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = SecondaryGreen,
    onPrimaryContainer = TextPrimary,
    secondary = SecondaryGreen,
    onSecondary = TextPrimary,
    tertiary = EarthAccent,
    onTertiary = SurfaceWhite,
    background = BackgroundCream,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = TextPrimary,
    outline = OutlineVariant,
    error = ErrorRed,
    onError = SurfaceWhite
)

@Composable
fun BotaniKTheme(content: @Composable () -> Unit) {
    val colorScheme = BotaniKColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        val activity = view.context as? android.app.Activity
        activity?.window?.let { window ->
            // `statusBarColor` está deprecado desde API 35: el color de la barra
            // de estado lo define el tema del sistema (edge-to-edge).
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BotaniKTypography,
        content = content
    )
}

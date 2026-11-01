package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuditDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceCardElevated,
    onPrimaryContainer = CyanNeon,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = CyanGlow,
    tertiary = PurpleAccent,
    onTertiary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderStroke,
    outlineVariant = BorderStrokeHover,
    error = SeverityCritical,
    onError = TextPrimary,
    errorContainer = SeverityCriticalBg,
    onErrorContainer = SeverityCritical
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We optimize for dark security terminal vibe
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BackgroundDark.toArgb()
                window.navigationBarColor = BackgroundDark.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AuditDarkColorScheme,
        typography = Typography,
        content = content
    )
}

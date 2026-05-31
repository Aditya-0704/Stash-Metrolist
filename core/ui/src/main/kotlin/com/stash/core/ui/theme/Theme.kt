package com.stash.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Color schemes ────────────────────────────────────────────────────────

val StashDarkColorScheme = darkColorScheme(
    primary = StashPurple,
    onPrimary = Color.White,
    primaryContainer = StashPurpleDark,
    onPrimaryContainer = StashPurpleLight,
    secondary = StashCyan,
    onSecondary = Color.Black,
    secondaryContainer = StashCyanDark,
    onSecondaryContainer = StashCyanLight,
    tertiary = StashCyan,
    onTertiary = Color.Black,
    background = StashBackground,
    onBackground = StashTextPrimary,
    surface = StashSurface,
    onSurface = StashTextPrimary,
    surfaceVariant = StashElevatedSurface,
    onSurfaceVariant = StashTextSecondary,
    error = StashError,
    onError = Color.White,
    outline = StashGlassBorder,
    outlineVariant = StashGlassBorderBright,
)

val StashLightColorScheme = lightColorScheme(
    primary = StashPurpleDark,                   // deeper purple on light bg for contrast
    onPrimary = Color.White,
    primaryContainer = StashPurpleLight,
    onPrimaryContainer = StashPurpleDark,
    secondary = StashCyanDark,
    onSecondary = Color.White,
    secondaryContainer = StashCyanLight,
    onSecondaryContainer = StashCyanDark,
    tertiary = StashCyanDark,
    onTertiary = Color.White,
    background = StashBackgroundLight,
    onBackground = StashTextPrimaryLight,
    surface = StashSurfaceLight,
    onSurface = StashTextPrimaryLight,
    surfaceVariant = StashElevatedSurfaceLight,
    onSurfaceVariant = StashTextSecondaryLight,
    error = StashError,
    onError = Color.White,
    outline = StashGlassBorderLight,
    outlineVariant = StashGlassBorderBrightLight,
)

// ── LocalIsDarkTheme — queried by composables that need theme-aware assets
//    (e.g. the wordmark drawable selection in HomeScreen). ────────────────
val LocalIsDarkTheme = staticCompositionLocalOf { true }

/**
 * Root theme for the Stash app.
 *
 * On Android 12+ (API 31+), uses Material You dynamic colors derived from
 * the user's wallpaper / cover art, giving the app a personalised feel
 * similar to Metrolist. Falls back to the handcrafted Stash palette on
 * older API levels.
 *
 * Accepts an explicit [darkTheme] flag so the caller can wire in a user
 * preference (Light/Dark/System) instead of always following the OS.
 * When the caller wants to mirror the system, pass
 * `isSystemInDarkTheme()` — that's also the default for safety.
 *
 * Switching between schemes is done purely in Compose state — no activity
 * recreation, no resource configuration override — so the flip is instant
 * and any `animateColorAsState` wrappers can animate between the two sets.
 *
 * The system status-bar and navigation-bar icon colors are updated via a
 * [SideEffect] so they flip in sync with the in-app theme.
 */
@Composable
fun StashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    seedColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val baseColorScheme = when {
        // Android 12+ (API 31): use wallpaper-derived dynamic colors
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Fallback to the handcrafted Stash palette
        darkTheme -> StashDarkColorScheme
        else -> StashLightColorScheme
    }

    val colorScheme = if (seedColor != null) {
        val darkBg = StashBackground.blend(seedColor, 0.08f)
        val darkSurface = StashSurface.blend(seedColor, 0.12f)
        val darkSurfaceVariant = StashElevatedSurface.blend(seedColor, 0.16f)

        val lightBg = StashBackgroundLight.blend(seedColor, 0.08f)
        val lightSurface = StashSurfaceLight.blend(seedColor, 0.12f)
        val lightSurfaceVariant = StashElevatedSurfaceLight.blend(seedColor, 0.16f)
        
        val isSeedLight = seedColor.luminance() > 0.4f
        val onSeed = if (isSeedLight) Color.Black else Color.White

        baseColorScheme.copy(
            primary = seedColor,
            primaryContainer = seedColor.copy(alpha = 0.3f),
            onPrimary = onSeed,
            secondary = seedColor,
            secondaryContainer = seedColor.copy(alpha = 0.3f),
            onSecondary = onSeed,
            tertiary = seedColor,
            tertiaryContainer = seedColor.copy(alpha = 0.3f),
            onTertiary = onSeed,
            background = if (darkTheme) darkBg else lightBg,
            onBackground = if (darkTheme) Color.White else Color.Black,
            surface = if (darkTheme) darkSurface else lightSurface,
            onSurface = if (darkTheme) Color.White else Color.Black,
            surfaceVariant = if (darkTheme) darkSurfaceVariant else lightSurfaceVariant,
            onSurfaceVariant = if (darkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
            outline = seedColor.copy(alpha = 0.2f),
            outlineVariant = seedColor.copy(alpha = 0.4f),
            surfaceContainerLowest = if (darkTheme) darkBg else lightBg,
            surfaceContainerLow = if (darkTheme) darkSurface else lightSurface,
            surfaceContainer = if (darkTheme) darkSurface else lightSurface,
            surfaceContainerHigh = if (darkTheme) darkSurfaceVariant else lightSurfaceVariant,
            surfaceContainerHighest = if (darkTheme) darkSurfaceVariant else lightSurfaceVariant,
        )
    } else {
        baseColorScheme
    }

    val extendedColors = if (seedColor != null) {
        val baseExt = if (darkTheme) StashExtendedColorsDark else StashExtendedColorsLight
        baseExt.copy(
            purpleLight = seedColor,
            purpleDark = seedColor.copy(alpha = 0.8f),
            cyan = seedColor,
            cyanLight = seedColor,
            cyanDark = seedColor.copy(alpha = 0.8f),
            glassBackground = seedColor.copy(alpha = 0.1f),
            glassBackgroundHover = seedColor.copy(alpha = 0.15f),
            glassBorder = seedColor.copy(alpha = 0.15f),
            glassBorderBright = seedColor.copy(alpha = 0.3f),
            textTertiary = if (darkTheme) seedColor.copy(alpha = 0.6f) else seedColor.copy(alpha = 0.8f),
            elevatedSurface = seedColor.copy(alpha = 0.15f),
        )
    } else {
        if (darkTheme) StashExtendedColorsDark else StashExtendedColorsLight
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Light status-bar icons on dark theme, dark icons on light theme.
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalStashColors provides extendedColors,
        LocalIsDarkTheme provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StashTypography,
            shapes = StashShapes,
            content = content,
        )
    }
}

object StashTheme {
    val extendedColors: StashExtendedColors
        @Composable
        get() = LocalStashColors.current
}

private fun Color.blend(other: Color, amount: Float): Color {
    val r = this.red + (other.red - this.red) * amount
    val g = this.green + (other.green - this.green) * amount
    val b = this.blue + (other.blue - this.blue) * amount
    val a = this.alpha + (other.alpha - this.alpha) * amount
    return Color(r, g, b, a)
}

package com.stash.feature.nowplaying.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware

/**
 * A full-bleed ambient background that renders a highly blurred version of the
 * album art, mimicking Metrolist's PlayerBackgroundStyle.BLUR.
 */
@Composable
fun AmbientBackground(
    artworkUri: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = artworkUri,
            transitionSpec = {
                fadeIn(tween(800)).togetherWith(fadeOut(tween(800)))
            },
            label = "ambientBackgroundBlur",
        ) { uri ->
            if (uri != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .size(100, 100) // Downsample heavily for blur
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(if (isDark) 150.dp else 100.dp)
                    )
                    // Dimming overlay so text remains readable
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f))
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF0F0F0)) // fallback
                )
            }
        }
    }
}

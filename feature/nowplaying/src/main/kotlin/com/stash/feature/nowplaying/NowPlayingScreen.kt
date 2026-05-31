package com.stash.feature.nowplaying


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.basicMarquee
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.isSystemInDarkTheme
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.stash.core.model.RepeatMode
import com.stash.core.model.isFlac
import com.stash.core.ui.components.SaveToPlaylistSheet
import com.stash.feature.nowplaying.ui.AmbientBackground
import com.stash.feature.nowplaying.ui.LyricsBottomSheet
import com.stash.feature.nowplaying.ui.QueueBottomSheet
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.HorizontalDivider

/**
 * Full-screen Now Playing screen with premium visual design.
 *
 * Displays album art with ambient background, playback controls, progress bar,
 * and track information. Colors are extracted from album art via Palette API.
 *
 * @param onDismiss Callback invoked when the user taps the dismiss (down arrow) button.
 * @param viewModel The [NowPlayingViewModel] provided by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onDismiss: () -> Unit,
    onNavigateToEq: () -> Unit,
    onNavigateToArtist: (String) -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val track = uiState.currentTrack
    var showQueue by remember { mutableStateOf(false) }
    var showSaveSheet by remember { mutableStateOf(false) }
    // "More Options" bottom sheet
    var showMoreOptions by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = isSystemInDarkTheme()

    DisposableEffect(view) {
        val activity = context as? Activity
        val window = activity?.window
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        
        var originalStatusBarColor = android.graphics.Color.TRANSPARENT
        var originalNavBarColor = android.graphics.Color.TRANSPARENT

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            
            originalStatusBarColor = window.statusBarColor
            originalNavBarColor = window.navigationBarColor
            
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }

        onDispose {
            activity?.requestedOrientation = originalOrientation
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                window.statusBarColor = originalStatusBarColor
                window.navigationBarColor = originalNavBarColor
            }
        }
    }

    // One-shot Toast confirmation for the "wrong match" flag action. Toast
    // instead of Snackbar so we don't have to restructure the screen into
    // a Scaffold — the full-screen ambient background would fight with
    // Material's Snackbar surface anyway.
    val toastContext = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { msg ->
            android.widget.Toast.makeText(toastContext, msg, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Queue bottom sheet
    if (showQueue) {
        QueueBottomSheet(
            queue = uiState.queue,
            currentIndex = uiState.currentIndex,
            accentColor = uiState.vibrantColor,
            onDismiss = { showQueue = false },
            onTrackClick = { index ->
                viewModel.onSkipToQueueIndex(index)
                showQueue = false
            },
            onRemoveTrack = viewModel::onRemoveFromQueue,
            onMoveTrack = viewModel::onMoveInQueue,
        )
    }

    // v0.9.36 Task 12 — lyrics bottom sheet. The IconButton that
    // toggles this lives in Task 13; until then, no UI affordance
    // triggers `onShowLyrics()`. The block below is the real wiring
    // that Task 13 will hook into.
    val showLyrics by viewModel.lyricsSheetOpen.collectAsStateWithLifecycle()
    if (showLyrics) {
        val lyricsState by viewModel.lyricsViewState.collectAsStateWithLifecycle()
        val lyricsPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
        LyricsBottomSheet(
            state = lyricsState,
            currentPositionMs = lyricsPositionMs,
            onSeek = viewModel::onLyricsLineSeek,
            onRetry = viewModel::onLyricsRetry,
            onDismiss = viewModel::onDismissLyrics,
        )
    }

    // Save to playlist bottom sheet
    if (showSaveSheet && track != null) {
        SaveToPlaylistSheet(
            playlists = uiState.userPlaylists,
            onSaveToPlaylist = { playlistId ->
                viewModel.saveTrackToPlaylist(track.id, playlistId)
            },
            onCreatePlaylist = { name ->
                viewModel.createPlaylistAndAddTrack(name, track.id)
            },
            onDismiss = { showSaveSheet = false },
        )
    }

    // "This song is wrong" — 3-option dialog triggered by the flag icon.
    // Separated from the icon's direct action so the same entry point
    // covers three very different outcomes: mark for replacement, delete
    // the file, delete + permanently block.
    if (showMoreOptions && track != null) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showMoreOptions = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "More Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { 
                        showMoreOptions = false
                        onNavigateToEq() 
                    }.padding(16.dp)
                ) {
                    Icon(painter = painterResource(com.stash.feature.nowplaying.R.drawable.equalizer), contentDescription = null, tint = uiState.vibrantColor)
                    Spacer(Modifier.width(16.dp))
                    Text("Equalizer")
                }
                // Removed Radio option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { 
                        showMoreOptions = false
                        onNavigateToArtist(track.artist)
                    }.padding(16.dp)
                ) {
                    Icon(painter = painterResource(com.stash.feature.nowplaying.R.drawable.artist), contentDescription = null, tint = uiState.vibrantColor)
                    Spacer(Modifier.width(16.dp))
                    Text("View Artist")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { 
                        showMoreOptions = false
                        viewModel.toggleDownloadForCurrentTrack()
                    }.padding(16.dp)
                ) {
                    Icon(painter = painterResource(com.stash.feature.nowplaying.R.drawable.download), contentDescription = null, tint = uiState.vibrantColor)
                    Spacer(Modifier.width(16.dp))
                    Text(if (track.isDownloaded) "Remove Download" else "Download")
                }
            }
        }
    }

    val innerPadding = com.stash.core.ui.LocalScaffoldPadding.current
    val topPadding = innerPadding.calculateTopPadding()
    val bottomPadding = innerPadding.calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .layout { measurable, constraints ->
                val topPx = topPadding.roundToPx()
                val bottomPx = bottomPadding.roundToPx()
                val placeable = measurable.measure(
                    constraints.copy(
                        maxHeight = if (constraints.hasBoundedHeight) constraints.maxHeight + topPx + bottomPx else constraints.maxHeight
                    )
                )
                layout(placeable.width, placeable.height) {
                    placeable.place(0, -topPx)
                }
            }
    ) {
        // Ambient animated background behind everything.
        AmbientBackground(
            artworkUri = track?.albumArtPath ?: track?.albumArtUrl,
            modifier = Modifier.matchParentSize()
        )

        // Main layout: NO verticalScroll — uses weight(1f) on album art
        // so controls are always on screen (Metrolist pattern).
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .layout { measurable, constraints ->
                    val topPx = topPadding.roundToPx()
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, topPx) // Push content down so it doesn't overlap camera hole
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // -- Minimal header: "Now Playing" + queue subtitle --
            MinimalHeader(
                onDismiss = onDismiss,
                queueInfo = if (uiState.queueSize > 1) "Your Queue" else null,
            )

            Spacer(modifier = Modifier.weight(1f))

            // -- Album art --
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(8f, fill = false),
                contentAlignment = Alignment.Center,
            ) {
                AlbumArtSection(
                    albumArtUrl = track?.albumArtUrl,
                    albumArtPath = track?.albumArtPath,
                    accentColor = uiState.vibrantColor,
                    onBitmapLoaded = viewModel::onAlbumArtLoaded,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // -- Track info & Inline Actions (M3 Expressive) --
            TrackInfoRow(
                track = track,
                uiState = uiState,
                onLikeTap = viewModel::onLikeTap,
                onShareClick = viewModel::onShowLyrics,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // -- Progress bar (M3 Slider with local state for no lag) --
            PlayerSlider(
                currentPositionMs = uiState.currentPositionMs,
                durationMs = uiState.durationMs,
                accentColor = uiState.vibrantColor,
                onSeek = viewModel::onSeekTo,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -- Playback controls (M3 Expressive with mutual shrink) --
            M3PlaybackControls(
                isPlaying = uiState.isPlaying,
                accentColor = uiState.vibrantColor,
                onPlayPauseClick = viewModel::onPlayPauseClick,
                onSkipNext = viewModel::onSkipNext,
                onSkipPrevious = viewModel::onSkipPrevious,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // -- Bottom action bar (Metrolist-style) --
            BottomActionBar(
                onQueueClick = { showQueue = true },
                onShuffleClick = viewModel::onToggleShuffle,
                onRepeatClick = viewModel::onCycleRepeatMode,
                onFlagClick = { showMoreOptions = true },
                onDownloadClick = viewModel::toggleDownloadForCurrentTrack,
                onSaveClick = { showSaveSheet = true },
                shuffleEnabled = uiState.shuffleEnabled,
                repeatMode = uiState.repeatMode,
                accentColor = uiState.vibrantColor,
                isDownloaded = uiState.currentTrack?.isDownloaded == true,
                hasTrack = uiState.hasTrack,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
fun hasTrack(track: com.stash.core.model.Track?): Boolean = track != null

// ---------------------------------------------------------------------------
// Private composables
// ---------------------------------------------------------------------------

/**
 * Minimal header matching Metrolist's design: centered "Now Playing" text
 * with optional "Your Queue" subtitle and a dismiss chevron.
 */
@Composable
private fun MinimalHeader(
    onDismiss: () -> Unit,
    queueInfo: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        // Dismiss button on the left (invisible tap target)
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dismiss",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }

        // Centered titles
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
            )
            if (queueInfo != null) {
                Text(
                    text = queueInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/**
 * Album art with a colored glow shadow behind it.
 *
 * Uses Coil 3 [AsyncImage] to load the art. When the image is loaded
 * successfully, the bitmap is forwarded to [onBitmapLoaded] for palette
 * extraction.
 */
@Composable
private fun AlbumArtSection(
    albumArtUrl: String?,
    albumArtPath: String?,
    accentColor: Color,
    onBitmapLoaded: (android.graphics.Bitmap?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val artModel = albumArtPath ?: albumArtUrl

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.aspectRatio(1f)
    ) {
        // Glow behind the artwork.
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .shadow(
                    elevation = 40.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = accentColor.copy(alpha = 0.3f),
                    spotColor = accentColor.copy(alpha = 0.3f),
                ),
        )

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(artModel)
                .allowHardware(false) // Required for Palette bitmap extraction.
                .build(),
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            onState = { state ->
                if (state is AsyncImagePainter.State.Success) {
                    try {
                        val bitmap = state.result.image.toBitmap()
                        onBitmapLoaded(bitmap)
                    } catch (_: Exception) {
                        // Bitmap extraction failed; palette will use defaults.
                        onBitmapLoaded(null)
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}

/**
 * Track info row: title + artist on the left, share and like buttons
 * on the right, matching Metrolist's asymmetric paired button design.
 */
@Composable
private fun TrackInfoRow(
    track: com.stash.core.model.Track?,
    uiState: NowPlayingUiState,
    onLikeTap: () -> Unit,
    onShareClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = track,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "title",
            ) { currentTrack ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentTrack?.title ?: "Not Playing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .basicMarquee(
                                iterations = 1,
                                initialDelayMillis = 3000,
                                velocity = 30.dp,
                            ),
                    )
                    
                    com.stash.core.ui.components.FlacBadge(
                        fileFormat = currentTrack?.fileFormat,
                        bitsPerSample = currentTrack?.bitsPerSample,
                        sampleRateHz = currentTrack?.sampleRateHz,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedContent(
                targetState = track?.artist ?: "",
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "artist",
            ) { artist ->
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(
                        iterations = 1,
                        initialDelayMillis = 3000,
                        velocity = 30.dp,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Metrolist-style asymmetric paired buttons (Share + Like)
        if (track != null) {
            val shareShape = RoundedCornerShape(
                topStart = 50.dp,
                bottomStart = 50.dp,
                topEnd = 3.dp,
                bottomEnd = 3.dp,
            )
            val favShape = RoundedCornerShape(
                topStart = 3.dp,
                bottomStart = 3.dp,
                topEnd = 50.dp,
                bottomEnd = 50.dp,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Lyrics button
                MetrolistIconButton(
                    onClick = onShareClick,
                    shape = shareShape,
                    iconRes = com.stash.feature.nowplaying.R.drawable.lyrics,
                    contentDescription = "Lyrics",
                    accentColor = uiState.vibrantColor,
                    isActive = false,
                )

                // Like button
                val isLiked = uiState.currentTrack?.stashLikedAt != null
                MetrolistIconButton(
                    onClick = onLikeTap,
                    shape = favShape,
                    iconRes = if (isLiked) com.stash.feature.nowplaying.R.drawable.favorite else com.stash.feature.nowplaying.R.drawable.favorite_border,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    accentColor = uiState.vibrantColor,
                    isActive = false,
                )
            }
        }
    }
}

/**
 * M3 Expressive Playback controls: previous, play/pause, next.
 *
 * Features spring animations with MUTUAL SHRINK behavior:
 * when one button is pressed, the other buttons shrink, creating
 * the breathing M3 Expressive feel from Metrolist.
 *
 * Exact Metrolist spring spec: dampingRatio = 0.6f, stiffness = 500f
 */
@Composable
private fun M3PlaybackControls(
    isPlaying: Boolean,
    accentColor: Color,
    onPlayPauseClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val backInteractionSource = remember { MutableInteractionSource() }
        val nextInteractionSource = remember { MutableInteractionSource() }
        val playPauseInteractionSource = remember { MutableInteractionSource() }

        val isPlayPausePressed by playPauseInteractionSource.collectIsPressedAsState()
        val isBackPressed by backInteractionSource.collectIsPressedAsState()
        val isNextPressed by nextInteractionSource.collectIsPressedAsState()

        // Metrolist's EXACT spring spec
        val springSpec = spring<Float>(dampingRatio = 0.6f, stiffness = 500f)

        // Play/Pause: idle=1.3f, selfPressed=1.9f, siblingPressed=1.1f
        val playPauseWeight by animateFloatAsState(
            targetValue = if (isPlayPausePressed) {
                1.9f
            } else if (isBackPressed || isNextPressed) {
                1.1f
            } else {
                1.3f
            },
            animationSpec = springSpec,
            label = "playPauseWeight",
        )

        // Previous: idle=0.45f, selfPressed=0.65f, siblingPressed=0.35f
        val backButtonWeight by animateFloatAsState(
            targetValue = if (isBackPressed) {
                0.65f
            } else if (isPlayPausePressed) {
                0.35f
            } else {
                0.45f
            },
            animationSpec = springSpec,
            label = "backButtonWeight",
        )

        // Next: idle=0.45f, selfPressed=0.65f, siblingPressed=0.35f
        val nextButtonWeight by animateFloatAsState(
            targetValue = if (isNextPressed) {
                0.65f
            } else if (isPlayPausePressed) {
                0.35f
            } else {
                0.45f
            },
            animationSpec = springSpec,
            label = "nextButtonWeight",
        )

        // Previous button
        FilledIconButton(
            onClick = onSkipPrevious,
            shape = RoundedCornerShape(50),
            interactionSource = backInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = accentColor.copy(alpha = 0.35f),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .height(68.dp)
                .weight(backButtonWeight)
                .scale(if (isBackPressed) 0.9f else 1f),
        ) {
            Icon(
                painter = painterResource(id = com.stash.feature.nowplaying.R.drawable.skip_previous),
                contentDescription = "Previous",
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Play/Pause button — Metrolist uses FilledIconButton with icon + text label
        FilledIconButton(
            onClick = onPlayPauseClick,
            shape = RoundedCornerShape(50),
            interactionSource = playPauseInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = accentColor,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .height(68.dp)
                .weight(playPauseWeight)
                .scale(if (isPlayPausePressed) 0.9f else 1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = if (isPlaying) painterResource(id = com.stash.feature.nowplaying.R.drawable.pause) else painterResource(id = com.stash.feature.nowplaying.R.drawable.play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlaying) "Pause" else "Play",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Next button
        FilledIconButton(
            onClick = onSkipNext,
            shape = RoundedCornerShape(50),
            interactionSource = nextInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = accentColor.copy(alpha = 0.35f),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .height(68.dp)
                .weight(nextButtonWeight)
                .scale(if (isNextPressed) 0.9f else 1f),
        ) {
            Icon(
                painter = painterResource(id = com.stash.feature.nowplaying.R.drawable.skip_next),
                contentDescription = "Next",
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

/**
 * Player slider with local state to prevent lag.
 *
 * Uses the Metrolist pattern: sliderPosition is updated locally during drag,
 * and only seeks the player on onValueChangeFinished.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSlider(
    currentPositionMs: Long,
    durationMs: Long,
    accentColor: Color,
    onSeek: (Long) -> Unit,
) {
    // Local slider position state — null means "not dragging, show real position"
    var sliderPosition by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = (sliderPosition ?: currentPositionMs).toFloat(),
            valueRange = 0f..(if (durationMs > 0) durationMs.toFloat() else 1f),
            onValueChange = { newValue ->
                sliderPosition = newValue.toLong()
            },
            onValueChangeFinished = {
                sliderPosition?.let { pos ->
                    onSeek(pos)
                }
                // Keep sliderPosition alive briefly so the UI doesn't
                // snap back to the old currentPositionMs before the
                // player catches up. It will be overwritten on next
                // recomposition when currentPositionMs updates.
            },
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        // Clear the slider position once the player catches up
        LaunchedEffect(currentPositionMs) {
            sliderPosition?.let { held ->
                // If the player has moved past (or near) where we seeked, release
                val diff = kotlin.math.abs(currentPositionMs - held)
                if (diff < 1500) sliderPosition = null
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDuration(sliderPosition ?: currentPositionMs),
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
            )
            Text(
                text = if (durationMs > 0) formatDuration(durationMs) else "",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * A custom icon button matching Metrolist's Box + padding design.
 */
@Composable
fun MetrolistIconButton(
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    iconRes: Int,
    contentDescription: String,
    accentColor: Color,
    isActive: Boolean = false,
) {
    val contentColor = if (isActive) Color.White else Color.White.copy(alpha = 0.8f)

    val appliedModifier = if (isActive) {
        Modifier
            .size(42.dp)
            .clip(shape)
            .background(accentColor)
            .clickable(onClick = onClick)
    } else {
        Modifier
            .size(42.dp)
            .clip(shape)
            .border(1.dp, accentColor, shape)
            .clickable(onClick = onClick)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = appliedModifier
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )
    }
}

/**
 * Bottom action bar matching Metrolist's queue collapsed content.
 *
 * Layout: [Queue] [Sleep] [Shuffle] [EQ] [Repeat]  ...  [⋮ More]
 * Uses asymmetric pill shapes for the first and last buttons.
 */
@Composable
private fun BottomActionBar(
    onQueueClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onFlagClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSaveClick: () -> Unit,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    accentColor: Color,
    isDownloaded: Boolean,
    hasTrack: Boolean,
) {
    // Metrolist asymmetric shapes
    val pillLeftShape = RoundedCornerShape(
        topStart = 50.dp,
        bottomStart = 50.dp,
        topEnd = 3.dp,
        bottomEnd = 3.dp,
    )
    val middleShape = RoundedCornerShape(3.dp)
    val pillRightShape = RoundedCornerShape(
        topStart = 3.dp,
        bottomStart = 3.dp,
        topEnd = 50.dp,
        bottomEnd = 50.dp,
    )

    val buttonColor = Color.Transparent
    val contentColor = Color.White.copy(alpha = 0.8f)
    val activeContentColor = Color.White

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Queue (pill-left)
            MetrolistIconButton(
                onClick = onQueueClick,
                shape = pillLeftShape,
                iconRes = com.stash.feature.nowplaying.R.drawable.queue_music,
                contentDescription = "Queue",
                accentColor = accentColor,
                isActive = false,
            )

            // Shuffle (middle)
            MetrolistIconButton(
                onClick = onShuffleClick,
                shape = middleShape,
                iconRes = com.stash.feature.nowplaying.R.drawable.shuffle,
                contentDescription = "Shuffle",
                accentColor = accentColor,
                isActive = shuffleEnabled,
            )

            // Save to playlist (middle)
            MetrolistIconButton(
                onClick = onSaveClick,
                shape = middleShape,
                iconRes = com.stash.feature.nowplaying.R.drawable.playlist_add,
                contentDescription = "Save to playlist",
                accentColor = accentColor,
                isActive = false,
            )

            // Repeat (pill-right)
            MetrolistIconButton(
                onClick = onRepeatClick,
                shape = pillRightShape,
                iconRes = when (repeatMode) {
                    RepeatMode.ONE -> com.stash.feature.nowplaying.R.drawable.repeat_one
                    else -> com.stash.feature.nowplaying.R.drawable.repeat
                },
                contentDescription = "Repeat",
                accentColor = accentColor,
                isActive = repeatMode != RepeatMode.OFF,
            )
        }

        // Report / flag (circular)
        if (hasTrack) {
            MetrolistIconButton(
                onClick = onFlagClick,
                shape = CircleShape,
                iconRes = com.stash.feature.nowplaying.R.drawable.more_horiz,
                contentDescription = "More options",
                accentColor = accentColor,
                isActive = false,
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%d:%02d", m, s)
}

/**
 * Formats a one-line quality summary for the Now Playing screen.
 *
 * Examples:
 *   - All four fields known:  `FLAC · 24-bit/96.0 kHz · 4233 kbps`
 *   - Codec + bitrate only:    `OPUS · 160 kbps`
 *   - Codec only:              `FLAC` (data not yet backfilled)
 *
 * Returns null only when the codec is blank — in that case the caller
 * should render no line at all.
 */
private fun trackQualityText(track: com.stash.core.model.Track): String? {
    // v0.9.13 fix: tracks downloaded before format-tracking was wired (pre-v0.9.11)
    // default to file_format = "opus" regardless of the actual codec — so a FLAC
    // file would render "OPUS · 4233 kbps", which is the source of "every track says
    // Opus" complaints. The Library Health backfill writes correct values from disk
    // but only when the user opens that screen. Cheap interim correction: if the
    // track has a downloaded filePath, prefer the file extension as canonical.
    val extension = track.filePath
        ?.takeIf { it.isNotBlank() }
        ?.substringAfterLast('.', missingDelimiterValue = "")
        ?.lowercase()
    val codec = when (extension) {
        "flac", "alac", "wav", "ape", "tta", "wv", "aiff" -> extension.uppercase()
        "opus", "m4a", "mp3", "ogg", "aac" -> extension.uppercase()
        else -> track.fileFormat.takeIf { it.isNotBlank() }?.uppercase() ?: return null
    }
    val bitDepth = track.bitsPerSample
    val sampleRateKHz = track.sampleRateHz?.let { it / 1000.0 }
    val bitrate = track.qualityKbps.takeIf { it > 0 }
    return buildList {
        add(codec)
        if (bitDepth != null && sampleRateKHz != null) {
            add("${bitDepth}-bit/${"%.1f".format(sampleRateKHz)} kHz")
        }
        if (bitrate != null) add("$bitrate kbps")
        // Flag the YouTube fallback so the user can tell when a track is
        // playing from yt-dlp/InnerTube extraction rather than Qobuz. The
        // codec ("AAC") alone doesn't convey this — Qobuz also serves AAC
        // at MP3_320 tier. Only the streamOrigin field distinguishes the
        // two. We don't badge "via Kennyy" / "via squid" because those
        // are the expected primary sources; only the lossy fallback
        // deserves a callout.
        if (track.streamOrigin == "youtube") add("via YT")
    }.joinToString(" · ")
}

/**
 * Renders the codec/bitrate quality line beneath the artist · album row.
 * When [isStreaming] is `true` a small wifi glyph is prefixed so the
 * user can tell at a glance that playback is coming from the network
 * rather than a local file. The icon picks up
 * [MaterialTheme.colorScheme.primary] so it stands out against the
 * white-on-ambient quality text without clashing with the album-art
 * palette.
 *
 * Centered as a Row so the prefix-icon variant stays visually balanced
 * with the icon-less variant — the original `Text(textAlign = Center)`
 * call is preserved when there is nothing to prefix.
 */
@Composable
private fun QualityLine(
    qualityText: String,
    isStreaming: Boolean,
) {
    if (isStreaming) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Streaming",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = qualityText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Text(
            text = qualityText,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "QualityLine — streaming",
    showBackground = true,
    backgroundColor = 0xFF101012,
)
@Composable
private fun PreviewQualityLineStreaming() {
    com.stash.core.ui.theme.StashTheme {
        QualityLine(
            qualityText = "OPUS \u00B7 160 kbps",
            isStreaming = true,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "QualityLine — local",
    showBackground = true,
    backgroundColor = 0xFF101012,
)
@Composable
private fun PreviewQualityLineLocal() {
    com.stash.core.ui.theme.StashTheme {
        QualityLine(
            qualityText = "FLAC \u00B7 24-bit/96.0 kHz \u00B7 4233 kbps",
            isStreaming = false,
        )
    }
}

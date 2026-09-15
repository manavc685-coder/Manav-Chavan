package com.example.ui.screens.player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.MediaItem
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.DarkSurfaceElevated
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun VideoPlayerScreen(
    media: MediaItem,
    isTrailer: Boolean = false,
    onBackClick: () -> Unit,
    onProgressUpdate: (Float, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Reference to underlying VideoView and MediaPlayer
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var retryTrigger by remember { mutableStateOf(0) }

    val videoSource = remember(media, isTrailer, retryTrigger) {
        val target = if (isTrailer) media.trailerUrl else media.videoUrl
        if (target.isNotBlank()) target else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    }

    // Auto-hide controls timer
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    // Periodic time & progress poller (safe against uninitialized / invalid states)
    LaunchedEffect(isPlaying, isBuffering, hasError) {
        while (true) {
            try {
                videoViewRef?.let { vv ->
                    if (!hasError && !isDraggingSlider) {
                        val pos = vv.currentPosition.toLong()
                        val dur = vv.duration.toLong()
                        if (dur > 0) {
                            durationMs = dur
                            currentPositionMs = pos.coerceIn(0L, dur)
                            sliderValue = (currentPositionMs.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                            onProgressUpdate(sliderValue, currentPositionMs)
                        }
                        if (vv.isPlaying != isPlaying) {
                            isPlaying = vv.isPlaying
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore transient state exceptions
            }
            delay(350)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            videoViewRef = null
            mediaPlayerRef = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("video_player_screen")
    ) {
        // Safe Android VideoView
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setVideoURI(Uri.parse(videoSource))
                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        isBuffering = false
                        hasError = false
                        val dur = duration.toLong()
                        if (dur > 0) {
                            durationMs = dur
                            // Resume safely if within video duration
                            if (!isTrailer && media.lastWatchedPositionMs in 1 until dur - 2000L) {
                                seekTo(media.lastWatchedPositionMs.toInt())
                            }
                        }
                        if (isMuted) {
                            try {
                                mp.setVolume(0f, 0f)
                            } catch (_: Exception) {}
                        }
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        showControls = true
                        sliderValue = 1f
                    }
                    setOnErrorListener { _, what, extra ->
                        isBuffering = false
                        isPlaying = false
                        hasError = true
                        errorMessage = "Unable to load video stream (Code: $what, $extra)"
                        true // Prevent default error alert dialog
                    }
                    videoViewRef = this
                }
            },
            update = { vv ->
                videoViewRef = vv
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering && !hasError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BrandCrimson,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        // Error State Overlay with Retry Button
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = BrandCrimson,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Playback Error",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (errorMessage.isNotBlank()) errorMessage else "Connection to stream interrupted.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            hasError = false
                            isBuffering = true
                            retryTrigger++
                            try {
                                videoViewRef?.setVideoURI(Uri.parse(videoSource))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Stream", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Animated Interactive Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73000000))
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xCC000000), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = media.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isTrailer) "Official Trailer" else media.genre,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    // Quality Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0x66FFFFFF),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "1080p FULL HD",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Center Play / Pause & Seek Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay 10s
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                try {
                                    val target = (vv.currentPosition - 10000).coerceAtLeast(0)
                                    vv.seekTo(target)
                                    currentPositionMs = target.toLong()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("player_rewind_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Play / Pause toggle
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(BrandCrimson)
                            .clickable {
                                videoViewRef?.let { vv ->
                                    try {
                                        if (vv.isPlaying) {
                                            vv.pause()
                                            isPlaying = false
                                        } else {
                                            vv.start()
                                            isPlaying = true
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            .testTag("player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Forward 10s
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                try {
                                    val dur = vv.duration
                                    val target = if (dur > 0) {
                                        (vv.currentPosition + 10000).coerceAtMost(dur)
                                    } else {
                                        vv.currentPosition + 10000
                                    }
                                    vv.seekTo(target)
                                    currentPositionMs = target.toLong()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("player_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Bottom Progress Bar & Volume & Fullscreen Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xE6000000))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Time and Progress Bar Slider
                    Slider(
                        value = if (isDraggingSlider) sliderValue else {
                            if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                        },
                        onValueChange = { newValue ->
                            isDraggingSlider = true
                            sliderValue = newValue
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            videoViewRef?.let { vv ->
                                try {
                                    val dur = vv.duration
                                    if (dur > 0) {
                                        val target = (sliderValue * dur).toInt()
                                        vv.seekTo(target)
                                        currentPositionMs = target.toLong()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = BrandCrimson,
                            activeTrackColor = BrandCrimson,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("player_progress_slider")
                    )

                    // Bottom info and utility actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timestamp: "01:24 / 09:56"
                        Text(
                            text = "${formatTime(currentPositionMs)} / ${formatTime(durationMs)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Volume / Mute Button
                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    try {
                                        val vol = if (isMuted) 0f else 1f
                                        mediaPlayerRef?.setVolume(vol, vol)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isMuted) "Unmute" else "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Fullscreen Toggle
                            IconButton(
                                onClick = {
                                    isFullscreen = !isFullscreen
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

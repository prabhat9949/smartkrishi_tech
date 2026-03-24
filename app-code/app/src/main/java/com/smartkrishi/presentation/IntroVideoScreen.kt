package com.smartkrishi.presentation

import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.CircleShape
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.smartkrishi.R
import kotlinx.coroutines.delay

private const val TAG = "SplashVideoScreen"

@OptIn(UnstableApi::class)
@Composable
fun SplashVideoScreen(onFinish: () -> Unit) {

    val context = LocalContext.current
    var navigateNow by remember { mutableStateOf(false) }
    var videoLoaded by remember { mutableStateOf(false) }

    // Circle size - increased for better visibility
    val circleSize = 360.dp

    // Animated scale for smooth entrance
    val infiniteTransition = rememberInfiniteTransition(label = "scale")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnimation"
    )

    // ExoPlayer setup with MUTED audio
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse(
                "android.resource://${context.packageName}/${R.raw.app_entry_video_customization}"
            )
            setMediaItem(MediaItem.fromUri(uri))

            // ✅ MUTE THE VIDEO - Set volume to 0
            volume = 0f

            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_OFF
            prepare()

            Log.d(TAG, "ExoPlayer initialized with muted audio")
        }
    }

    fun goNext() {
        if (!navigateNow) {
            navigateNow = true
            Log.d(TAG, "Video completed, preparing to navigate")
            exoPlayer.release()
        }
    }

    // After video ends, navigate forward
    if (navigateNow) {
        LaunchedEffect(Unit) {
            delay(300) // Smooth transition delay
            Log.d(TAG, "Navigating to next screen")
            onFinish()
        }
    }

    // ExoPlayer Listener
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        videoLoaded = true
                        Log.d(TAG, "Video ready to play")
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "Video playback ended")
                        goNext()
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "Video buffering...")
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Video Error: ${error.message}", error)
                goNext()
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            Log.d(TAG, "Disposing ExoPlayer")
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Pure white background
        contentAlignment = Alignment.Center
    ) {

        // Circular video container with animation
        Box(
            modifier = Modifier
                .size(circleSize)
                .scale(if (videoLoaded) scale else 1f)
                .clip(CircleShape)
                .background(Color.White)
        ) {

            AndroidView(
                modifier = Modifier
                    .fillMaxSize(), // Fill the circular container
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        player = exoPlayer

                        // ✅ RESIZE_MODE_FIT to show full content without cropping
                        // This ensures "SMARTKRISHI" text is fully visible
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                        // Transparent background
                        setBackgroundColor(TRANSPARENT)
                        setShutterBackgroundColor(TRANSPARENT)

                        Log.d(TAG, "PlayerView created with RESIZE_MODE_FIT")
                    }
                }
            )
        }

        // Optional: Add a subtle circular border
        Box(
            modifier = Modifier
                .size(circleSize + 4.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
        )
    }
}

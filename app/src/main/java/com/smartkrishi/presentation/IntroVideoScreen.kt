package com.smartkrishi.presentation

import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.smartkrishi.R

private const val TAG = "IntroVideoScreen"
private val PrimaryGreen = Color(0xFF2E7D32)
@OptIn(UnstableApi::class)
@Composable
fun SplashVideoScreen(onFinish: () -> Unit) {

    val context = LocalContext.current
    var videoReady by remember { mutableStateOf(false) }
    var navigationTriggered by remember { mutableStateOf(false) }

    val circleSize = 360.dp

    val infiniteTransition = rememberInfiniteTransition(label = "introVideo")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnim"
    )

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse(
                "android.resource://${context.packageName}/${R.raw.app_entry_video_customization}"
            )
            setMediaItem(MediaItem.fromUri(uri))
            volume = 0f
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
        }
    }

    DisposableEffect(Unit) {

        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {

                    Player.STATE_READY -> {
                        videoReady = true
                    }

                    Player.STATE_ENDED -> {
                        if (!navigationTriggered) {
                            navigationTriggered = true
                            onFinish()   // 🔥 trigger navigation
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (!navigationTriggered) {
                    navigationTriggered = true
                    onFinish()
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // 🚫 DO NOT return early anymore
    // Keep UI rendered until navigation fully happens

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(circleSize)
                .scale(scale)
                .clip(CircleShape)
                .background(Color.White)
        ) {

            AnimatedVisibility(
                visible = videoReady,
                enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.95f),
                exit = fadeOut()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            player = exoPlayer
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            setBackgroundColor(TRANSPARENT)
                            setShutterBackgroundColor(TRANSPARENT)
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = !videoReady,
                exit = fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "SmartKrishi logo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "SmartKrishi",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AI Powered Crop Health",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    CircularProgressIndicator(
                        color = Color(0xFF4CAF50),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(circleSize + 6.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryGreen.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
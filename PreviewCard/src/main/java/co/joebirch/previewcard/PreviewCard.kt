package co.joebirch.previewcard

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ClippingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@UnstableApi
@Composable
fun PreviewCard(
    modifier: Modifier = Modifier,
    cardWidth: Dp,
    cardHeight: Dp,
    videoUrl: String,
    hasFocus: Boolean,
    clipStartPosition: Long = 2000000,
    clipEndPosition: Long = 8000000,
    thumbnailFrame: Long = 5000000,
    thumbnailUrl: String? = null
) {
    Card(
        modifier = modifier
            .width(if (hasFocus) cardWidth.times(1.2f) else cardWidth)
            .height(if (hasFocus) cardHeight.times(1.2f) else cardHeight)
    ) {
        val context = LocalContext.current
        var currentlyPlaying by remember { mutableStateOf(false) }
        var isBuffering by remember { mutableStateOf(true) }


        var thumbnailBitmap by rememberSaveable {
            mutableStateOf<Bitmap?>(null)
        }

        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = REPEAT_MODE_ONE
                volume = 0f
                videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
        }

        exoPlayer.addListener(object : Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                currentlyPlaying = playbackState == Player.STATE_READY
            }
        })

        LaunchedEffect(key1 = videoUrl) {
            val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUrl))
            val clip = ClippingMediaSource(mediaSource, clipStartPosition, clipEndPosition)
            exoPlayer.setMediaSource(clip)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            exoPlayer.addListener(object : Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    isBuffering = playbackState == Player.STATE_BUFFERING
                }
            })
        }

        val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
        DisposableEffect(lifecycleOwner) {
            val lifecycle = lifecycleOwner.lifecycle
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        exoPlayer.playWhenReady = false
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        exoPlayer.playWhenReady = true
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        exoPlayer.run {
                            stop()
                            release()
                        }
                    }
                    else -> {}
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        Crossfade(targetState = hasFocus, animationSpec = tween(durationMillis = 700)) { focused ->
            if (focused) {
                Box {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize(),
                        factory = {
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                                controllerAutoShow = false
                                setKeepContentOnPlayerReset(true)
                                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                            }
                        }
                    )
                    if (isBuffering) {
                        PreviewThumbnail(
                            thumbnailUrl,
                            thumbnailBitmap,
                            thumbnailFrame,
                            videoUrl,
                            true
                        ) {
                            thumbnailBitmap = it
                        }
                    }
                }
                exoPlayer.play()
            } else {
                PreviewThumbnail(thumbnailUrl, thumbnailBitmap, thumbnailFrame, videoUrl, false) {
                    thumbnailBitmap = it
                }
            }
        }
    }
}

@Composable
fun PreviewThumbnail(
    thumbnail: String?,
    thumbnailBitmap: Bitmap?,
    thumbnailFrame: Long,
    videoUrl: String,
    isLoadingVideo: Boolean,
    onBitmapLoaded: (bitmap: Bitmap?) -> Unit
) {
    if (thumbnail == null && thumbnailBitmap == null) {
        LaunchedEffect(videoUrl) {
            withContext(Dispatchers.IO) {
                MediaMetadataRetriever().apply {
                    setDataSource(videoUrl, HashMap())
                }.also {
                    val result = it.getFrameAtTime(
                        thumbnailFrame,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    onBitmapLoaded(result)
                    it.release()
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .border(2.dp, Color.White.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnailBitmap != null) {
                Image(
                    painter = rememberAsyncImagePainter(thumbnailBitmap),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (isLoadingVideo) {
                CircularProgressIndicator(strokeWidth = 4.dp)
            }
        }
    }
}
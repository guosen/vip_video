package com.guosen.vipvideo.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.guosen.vipvideo.appContainer
import com.guosen.vipvideo.core.data.local.WatchHistoryEntity
import com.guosen.vipvideo.core.model.Episode
import com.guosen.vipvideo.core.network.VodApiConfig
import com.guosen.vipvideo.ui.theme.VipVideoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val vodId = intent.getIntExtra(EXTRA_VOD_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val poster = intent.getStringExtra(EXTRA_POSTER)
        val startIndex = intent.getIntExtra(EXTRA_EPISODE_INDEX, 0)
        val directUrl = intent.getStringExtra(EXTRA_DIRECT_URL)

        setContent {
            VipVideoTheme {
                PlayerScreen(
                    vodId = vodId,
                    title = title,
                    poster = poster,
                    startIndex = startIndex,
                    directUrl = directUrl,
                    onBack = { finish() },
                    onBindPlayer = { exo -> player = exo },
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_VOD_ID = "vod_id"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_POSTER = "poster"
        private const val EXTRA_EPISODE_INDEX = "episode_index"
        private const val EXTRA_DIRECT_URL = "direct_url"

        fun intent(
            context: Context,
            vodId: Int,
            title: String,
            poster: String?,
            episodeIndex: Int,
            episodes: List<Episode> = emptyList(),
        ): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_VOD_ID, vodId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_POSTER, poster)
                putExtra(EXTRA_EPISODE_INDEX, episodeIndex)
            }
        }

        fun intentForUrl(context: Context, title: String, playUrl: String): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DIRECT_URL, playUrl)
                putExtra(EXTRA_EPISODE_INDEX, 0)
            }
        }
    }
}

@Composable
private fun PlayerScreen(
    vodId: Int,
    title: String,
    poster: String?,
    startIndex: Int,
    directUrl: String?,
    onBack: () -> Unit,
    onBindPlayer: (ExoPlayer) -> Unit,
) {
    val context = LocalContext.current
    val container = context.appContainer()
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(startIndex) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vodId, directUrl) {
        if (directUrl.isNullOrBlank()) {
            episodes = container.repository.episodes(vodId)
            currentIndex = startIndex.coerceIn(0, (episodes.size - 1).coerceAtLeast(0))
        }
    }

    val playUrl = directUrl ?: episodes.getOrNull(currentIndex)?.url

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Text(
                text = episodes.getOrNull(currentIndex)?.name?.ifBlank { title } ?: title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            if (playUrl.isNullOrBlank()) {
                Text("无可播放地址", color = Color.White, modifier = Modifier.align(Alignment.Center))
            } else {
                VideoPlayer(
                    url = playUrl,
                    onError = { error = it },
                    onBindPlayer = onBindPlayer,
                    onProgress = { pos, dur ->
                        if (vodId <= 0) return@VideoPlayer
                        val ep = episodes.getOrNull(currentIndex) ?: return@VideoPlayer
                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            container.dao.upsertHistory(
                                WatchHistoryEntity(
                                    vodId = vodId,
                                    title = title,
                                    poster = poster,
                                    episodeIndex = currentIndex,
                                    episodeName = ep.name,
                                    positionMs = pos,
                                    durationMs = dur,
                                    updatedAt = System.currentTimeMillis(),
                                ),
                            )
                        }
                    },
                )
            }
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                )
            }
        }
        if (episodes.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(episodes) { index, ep ->
                    AssistChip(
                        onClick = { currentIndex = index },
                        label = { Text(ep.name.ifBlank { "第${index + 1}集" }) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoPlayer(
    url: String,
    onError: (String) -> Unit,
    onBindPlayer: (ExoPlayer) -> Unit,
    onProgress: (Long, Long) -> Unit,
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("VipVideo-Android/1.0")
            .setDefaultRequestProperties(
                mapOf(
                    "Referer" to VodApiConfig.DEFAULT_REFERER,
                    "Origin" to VodApiConfig.DEFAULT_REFERER.trimEnd('/'),
                ),
            )
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory))
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    DisposableEffect(url) {
        onBindPlayer(exoPlayer)
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        onDispose { exoPlayer.stop() }
    }

    LaunchedEffect(exoPlayer) {
        while (isActive) {
            onProgress(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
            delay(5_000)
        }
    }

    LaunchedEffect(exoPlayer) {
        // no-op listener placeholder; errors surface via player state in future iteration
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

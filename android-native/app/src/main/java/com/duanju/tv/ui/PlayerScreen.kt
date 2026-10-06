package com.duanju.tv.ui

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.duanju.tv.App
import com.duanju.tv.core.DramaDetail
import com.duanju.tv.core.PlaybackPlan
import com.duanju.tv.core.WatchEntry
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    detail: DramaDetail,
    startIndex: Int,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var index by remember(detail.drama.id, startIndex) {
        mutableStateOf(startIndex.coerceIn(0, (detail.episodes.size - 1).coerceAtLeast(0)))
    }
    var plan by remember { mutableStateOf<PlaybackPlan?>(null) }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var reload by remember { mutableStateOf(0) }

    LaunchedEffect(index, reload) {
        if (detail.episodes.isEmpty()) {
            plan = null
            loading = false
            error = "本剧暂无可播放的剧集"
            return@LaunchedEffect
        }
        loading = true
        error = ""
        plan = null
        try {
            plan = App.repository.resolve(detail.drama, detail.episodes[index])
        } catch (failure: Throwable) {
            error = failure.message ?: "无法获取播放地址"
        } finally {
            loading = false
        }
    }

    DisposableEffect(plan) {
        val active = plan
        val episodeNumber = index + 1
        val built = active?.let { buildPlayer(context, it) }
        player = built
        onDispose {
            player = null
            built?.let { exo ->
                val position = exo.currentPosition
                val duration = exo.duration
                exo.release()
                if (position > 0) {
                    App.library.saveProgress(
                        WatchEntry(
                            drama = detail.drama,
                            episode = episodeNumber,
                            positionMs = position,
                            durationMs = duration.coerceAtLeast(0),
                            updatedAt = System.currentTimeMillis(),
                        ),
                    )
                }
            }
            active?.let { current -> App.scope.launch { App.repository.release(current.session) } }
        }
    }

    DisposableEffect(Unit) {
        onDispose { App.scope.launch { App.repository.cancelPlayback() } }
    }

    Column(Modifier.fillMaxSize()) {
        HeaderRow(title = detail.drama.title) {
            TextButton(onClick = onBack) { Text("返回") }
        }

        when {
            error.isNotEmpty() -> ErrorBox(error, onRetry = { reload += 1 })
            loading || plan == null -> LoadingBox()
            else -> {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    val exo = player
                    if (exo == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        PlayerSurface(exo)
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PlayerActionButton(
                        text = "上一集",
                        enabled = index > 0,
                        onClick = { index -= 1 },
                    )
                    Text(
                        text = "${detail.episodes[index].title} · ${index + 1}/${detail.episodes.size}",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    PlayerActionButton(
                        text = "下一集",
                        enabled = index < detail.episodes.size - 1,
                        onClick = { index += 1 },
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerSurface(exo: ExoPlayer) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = true
                setShowNextButton(false)
                setShowPreviousButton(false)
                keepScreenOn = true
                player = exo
            }
        },
        update = { view -> view.player = exo },
        modifier = Modifier.fillMaxSize(),
    )
}

/** 站源播放地址通常需要携带 Referer/UA 等请求头，交由 Media3 数据源统一附加。 */
@OptIn(UnstableApi::class)
private fun buildPlayer(context: Context, plan: PlaybackPlan): ExoPlayer {
    val http = DefaultHttpDataSource.Factory()
    if (plan.headers.isNotEmpty()) {
        http.setDefaultRequestProperties(plan.headers)
    }
    http.setAllowCrossProtocolRedirects(true)
    http.setConnectTimeoutMs(15000)
    http.setReadTimeoutMs(20000)
    val player = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(http))
        .build()
    player.setHandleAudioBecomingNoisy(true)
    player.setMediaItem(MediaItem.fromUri(plan.url))
    player.playWhenReady = true
    player.prepare()
    return player
}

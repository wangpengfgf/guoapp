package com.duanju.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duanju.tv.App
import com.duanju.tv.core.Drama
import com.duanju.tv.core.DramaDetail
import com.duanju.tv.core.Sources

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(drama: Drama, onBack: () -> Unit, onPlay: (DramaDetail, Int) -> Unit) {
    var detail by remember(drama.id) { mutableStateOf<DramaDetail?>(null) }
    var loading by remember(drama.id) { mutableStateOf(true) }
    var error by remember(drama.id) { mutableStateOf("") }
    var reload by remember(drama.id) { mutableStateOf(0) }
    var favorite by remember(drama.id) { mutableStateOf(App.library.isFavorite(drama.id)) }

    LaunchedEffect(drama.id, reload) {
        loading = true
        error = ""
        try {
            detail = App.repository.detail(drama)
        } catch (failure: Throwable) {
            error = failure.message ?: "加载失败，请重试"
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("返回") }
            Text(
                drama.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            TextButton(
                onClick = {
                    favorite = App.library.toggleFavorite(detail?.drama ?: drama)
                },
            ) {
                Text(if (favorite) "已收藏" else "收藏")
            }
        }

        when {
            loading -> LoadingBox()
            error.isNotEmpty() -> ErrorBox(error, onRetry = { reload += 1 })
            detail != null -> {
                val current = detail!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    item { DetailHeader(current.drama) }
                    item { SectionTitle("选集 · 共${current.episodes.size}集") }
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            current.episodes.forEachIndexed { index, episode ->
                                EpisodeChip(
                                    title = episode.title,
                                    selected = false,
                                    onClick = { onPlay(current, index) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(drama: Drama) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .width(120.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                CoverImage(drama, Modifier.fillMaxSize())
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    drama.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    Sources.nameOf(drama.source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (drama.category.isNotEmpty()) {
                    Text(
                        drama.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    buildString {
                        if (drama.episodes > 0) append("共${drama.episodes}集 · ")
                        append(drama.releaseLabel())
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (drama.views.isNotEmpty()) {
                    Text(
                        "播放 ${drama.views}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (drama.description.isNotEmpty()) {
            Text(
                drama.description,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun Drama.releaseLabel(): String = when (releaseStatus) {
    "finished", "completed" -> "已完结"
    "ongoing" -> "连载中"
    else -> "状态未知"
}

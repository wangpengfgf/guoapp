package com.duanju.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duanju.tv.App
import com.duanju.tv.core.Drama
import com.duanju.tv.core.WatchEntry

@Composable
fun LibraryScreen(onOpen: (Drama) -> Unit) {
    var favorites by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var history by remember { mutableStateOf<List<WatchEntry>>(emptyList()) }
    var reload by remember { mutableStateOf(0) }

    LaunchedEffect(reload) {
        favorites = App.library.favorites()
        history = App.library.history()
    }

    Column(Modifier.fillMaxSize()) {
        HeaderRow(title = "收藏")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { SectionTitle("我的收藏") }
            item {
                if (favorites.isEmpty()) {
                    EmptyHint("还没有收藏的短剧")
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(favorites) { drama ->
                            PosterCard(
                                drama = drama,
                                onClick = { onOpen(drama) },
                                modifier = Modifier.width(116.dp),
                            )
                        }
                    }
                }
            }
            item { SectionTitle("观看记录") }
            if (history.isEmpty()) {
                item { EmptyHint("还没有观看记录") }
            } else {
                items(history) { entry ->
                    HistoryRow(
                        entry = entry,
                        onOpen = { onOpen(entry.drama) },
                        onRemove = {
                            App.library.removeProgress(entry.drama.id)
                            reload += 1
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HistoryRow(entry: WatchEntry, onOpen: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(end = 8.dp),
        ) {
            Text(
                entry.drama.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            val percent = if (entry.durationMs > 0) {
                (entry.positionMs * 100 / entry.durationMs).coerceIn(0, 100)
            } else {
                0
            }
            Text(
                "第${entry.episode}集 · 已看 $percent%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onOpen) { Text("继续观看") }
        TextButton(onClick = onRemove) { Text("删除") }
    }
}

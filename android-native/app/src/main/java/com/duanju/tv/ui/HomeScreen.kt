package com.duanju.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duanju.tv.App
import com.duanju.tv.core.CatalogCategory
import com.duanju.tv.core.Drama
import com.duanju.tv.core.Sources
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onOpen: (Drama) -> Unit) {
    val sources = remember { Sources.all }
    var source by rememberSaveable { mutableStateOf(sources.first().id) }
    var categories by remember { mutableStateOf<List<CatalogCategory>>(emptyList()) }
    var category by remember { mutableStateOf("") }
    var dramas by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var reload by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    LaunchedEffect(source) {
        category = ""
        categories = listOf(CatalogCategory("", "全部")) + runCatching {
            App.repository.categories(source)
        }.getOrDefault(emptyList())
    }

    LaunchedEffect(source, category, reload) {
        loading = true
        error = ""
        dramas = emptyList()
        hasMore = false
        page = 1
        try {
            val result = App.repository.catalog(source, page = 1, category = category)
            dramas = result.items
            hasMore = result.hasMore
        } catch (failure: Throwable) {
            error = failure.message ?: "加载失败，请重试"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(source, category) {
        gridState.scrollToItem(0)
    }

    fun loadMore() {
        if (loadingMore || loading || !hasMore) return
        val requestSource = source
        val requestCategory = category
        val next = page + 1
        loadingMore = true
        scope.launch {
            try {
                val result = App.repository.catalog(requestSource, page = next, category = requestCategory)
                if (requestSource == source && requestCategory == category) {
                    dramas = dramas + result.items
                    page = next
                    hasMore = result.hasMore
                }
            } catch (_: Throwable) {
            } finally {
                loadingMore = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        HeaderRow(title = "短剧TV")
        SourceSelector(sources = sources, selected = source, onSelect = { source = it })
        CategorySelector(categories = categories, selected = category, onSelect = { category = it })

        when {
            loading -> LoadingBox()
            error.isNotEmpty() -> ErrorBox(error, onRetry = { reload += 1 })
            dramas.isEmpty() -> Box(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("暂无内容", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 116.dp),
                state = gridState,
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(dramas) { drama ->
                    PosterCard(drama, onClick = { onOpen(drama) })
                }
                if (hasMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            TextButton(onClick = { loadMore() }, enabled = !loadingMore) {
                                Text(if (loadingMore) "加载中…" else "加载更多")
                            }
                        }
                    }
                }
            }
        }
    }
}

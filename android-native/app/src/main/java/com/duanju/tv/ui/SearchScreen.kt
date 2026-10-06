package com.duanju.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.duanju.tv.App
import com.duanju.tv.core.Drama
import com.duanju.tv.core.Sources

@Composable
fun SearchScreen(onOpen: (Drama) -> Unit) {
    val sources = remember { Sources.all }
    var source by rememberSaveable { mutableStateOf(sources.first().id) }
    var input by rememberSaveable { mutableStateOf("") }
    var submitted by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var reload by remember { mutableStateOf(0) }

    fun submit() {
        submitted = input.trim()
        reload += 1
    }

    LaunchedEffect(source, submitted, reload) {
        if (submitted.isEmpty()) {
            results = emptyList()
            error = ""
            return@LaunchedEffect
        }
        loading = true
        error = ""
        results = emptyList()
        try {
            results = App.repository.catalog(source, query = submitted).items
        } catch (failure: Throwable) {
            error = failure.message ?: "搜索失败，请重试"
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        HeaderRow(title = "搜索")
        SourceSelector(sources = sources, selected = source, onSelect = { source = it })
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("剧名关键词") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit() }),
            )
            Button(onClick = { submit() }) { Text("搜索") }
        }

        when {
            loading -> LoadingBox()
            error.isNotEmpty() -> ErrorBox(error, onRetry = { reload += 1 })
            submitted.isEmpty() -> Box(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("输入关键词后开始搜索", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            results.isEmpty() -> Box(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("没有找到相关内容", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 116.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(results) { drama ->
                    PosterCard(drama, onClick = { onOpen(drama) })
                }
            }
        }
    }
}

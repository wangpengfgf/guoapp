package com.duanju.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.duanju.tv.core.Drama
import com.duanju.tv.core.DramaDetail

/** 首页 / 搜索 / 收藏为一级页面，详情与播放页叠加在其上。 */
@Composable
fun AppRoot() {
    val stack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val current = stack.last()

    val openDetail: (Drama) -> Unit = { stack.add(Screen.Detail(it)) }
    val openPlayer: (DramaDetail, Int) -> Unit = { detail, index ->
        stack.add(Screen.Player(detail, index))
    }
    val goBack: () -> Unit = {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
    val switchTab: (Screen) -> Unit = { tab ->
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
        if (stack[0] != tab) stack[0] = tab
    }

    BackHandler(enabled = stack.size > 1, onBack = goBack)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val active = current
            if (active == Screen.Home || active == Screen.Search || active == Screen.Library) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = active == Screen.Home,
                        onClick = { switchTab(Screen.Home) },
                        icon = {},
                        label = { Text("首页") },
                    )
                    NavigationBarItem(
                        selected = active == Screen.Search,
                        onClick = { switchTab(Screen.Search) },
                        icon = {},
                        label = { Text("搜索") },
                    )
                    NavigationBarItem(
                        selected = active == Screen.Library,
                        onClick = { switchTab(Screen.Library) },
                        icon = {},
                        label = { Text("收藏") },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (current) {
                Screen.Home -> HomeScreen(onOpen = openDetail)
                Screen.Search -> SearchScreen(onOpen = openDetail)
                Screen.Library -> LibraryScreen(onOpen = openDetail)
                is Screen.Detail -> DetailScreen(
                    drama = current.drama,
                    onBack = goBack,
                    onPlay = openPlayer,
                )
                is Screen.Player -> PlayerScreen(
                    detail = current.detail,
                    startIndex = current.index,
                    onBack = goBack,
                )
            }
        }
    }
}

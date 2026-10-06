package com.duanju.tv.ui

import com.duanju.tv.core.Drama
import com.duanju.tv.core.DramaDetail

sealed interface Screen {
    data object Home : Screen
    data object Search : Screen
    data object Library : Screen
    data class Detail(val drama: Drama) : Screen
    data class Player(val detail: DramaDetail, val index: Int) : Screen
}

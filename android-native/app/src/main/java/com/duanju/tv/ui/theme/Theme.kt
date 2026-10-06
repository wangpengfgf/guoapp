package com.duanju.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DuanjuColors = darkColorScheme(
    primary = Color(0xFFFF5252),
    onPrimary = Color.White,
    background = Color(0xFF111318),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF171A21),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF232734),
    onSurfaceVariant = Color(0xFFB9BEC9),
)

@Composable
fun DuanjuTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DuanjuColors, content = content)
}

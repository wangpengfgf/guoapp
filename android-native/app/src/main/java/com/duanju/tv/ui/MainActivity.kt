package com.duanju.tv.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.duanju.tv.App
import com.duanju.tv.ui.theme.DuanjuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.init(applicationContext)
        setContent {
            DuanjuTheme {
                AppRoot()
            }
        }
    }
}

package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.chess.model.initialBitboards
import com.example.chess.ui.ChessBoardBitboard
import com.example.chess.ui.MenuScreen
import com.example.chess.ui.WatchGameScreen

sealed class Screen {
    data object Menu : Screen()
    data object Game : Screen()
    data object Watch : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    var screen by remember { mutableStateOf<Screen>(Screen.Menu) }

                    Box(Modifier.fillMaxSize()) {
                        when (screen) {
                            Screen.Menu -> MenuScreen(
                                onGetStarted = { screen = Screen.Game },
                                onWatchGame = { screen = Screen.Watch }
                            )
                            Screen.Game -> ChessBoardBitboard(
                                initial = initialBitboards(),
                                onBack = { screen = Screen.Menu },
                                modifier = Modifier.fillMaxSize()
                            )
                            Screen.Watch -> WatchGameScreen(
                                onBack = { screen = Screen.Menu }
                            )
                        }
                    }
                }
            }
        }
    }
}

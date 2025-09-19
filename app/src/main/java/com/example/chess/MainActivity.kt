package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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

                    AnimatedContent(
                        targetState = screen,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            val animationDuration = 400
                            val slideDirection = when (targetState) {
                                Screen.Menu -> -1 // Slide in from left when going back to menu
                                Screen.Game, Screen.Watch -> 1 // Slide in from right when going forward
                            }
                            
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> slideDirection * fullWidth },
                                animationSpec = tween(animationDuration)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -slideDirection * fullWidth },
                                animationSpec = tween(animationDuration)
                            )
                        },
                        label = "ScreenTransition"
                    ) { currentScreen ->
                        when (currentScreen) {
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

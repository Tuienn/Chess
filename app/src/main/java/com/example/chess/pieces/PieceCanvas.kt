// com/example/chess/pieces/PieceCanvas.kt
package com.example.chess.pieces

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
internal fun PieceCanvas(
    modifier: Modifier,
    draw: DrawScope.() -> Unit
) = Canvas(modifier = modifier, onDraw = draw)

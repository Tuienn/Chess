// com/example/chess/pieces/Pieces.kt
package com.example.chess.pieces

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
enum class PieceSide { White, Black }

@Stable
data class PieceColors(
    val fill: Color,      // màu thân
    val stroke: Color,    // màu viền / chi tiết
    val accent: Color = stroke // có thể dùng cho họa tiết
)

object PieceTheme {
    val White = PieceColors(fill = Color(0xFFF7F7F7), stroke = Color(0xFF111111))
    val Black = PieceColors(fill = Color(0xFF111111), stroke = Color(0xFFF7F7F7))
}

fun colorsFor(side: PieceSide) = when (side) {
    PieceSide.White -> PieceTheme.White
    PieceSide.Black -> PieceTheme.Black
}



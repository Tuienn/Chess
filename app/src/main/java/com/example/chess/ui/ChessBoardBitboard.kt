package com.example.chess.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.chess.model.Bitboards
import com.example.chess.model.rowColFromIndex
import com.example.chess.model.squaresFrom

/** Render toàn bộ bàn cờ từ 12 bitboard và overlay Piece. */
@Composable
fun ChessBoardBitboard(
    bitboards: Bitboards,
    modifier: Modifier = Modifier,
    lightColor: Color = Color(0xFFEEEED2),
    darkColor: Color = Color(0xFF769656)
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val side = maxWidth     // dp
        val square = side / 8   // dp

        Box(Modifier.size(side)) {
            ChessBoardCanvas(
                modifier = Modifier.matchParentSize(),
                lightColor = lightColor,
                darkColor = darkColor
            )

            // Duyệt từng bitboard → set bit → (row,col) → Piece(code, row, col)
            @Composable
            fun emitPieces(bb: ULong, code: String) {
                for (idx in squaresFrom(bb)) {
                    val (row, col) = rowColFromIndex(idx)
                    Piece(code = code, row = row, col = col, square = square)
                }
            }

            // White
            emitPieces(bitboards.WK, "wk")
            emitPieces(bitboards.WQ, "wq")
            emitPieces(bitboards.WR, "wr")
            emitPieces(bitboards.WB, "wb")
            emitPieces(bitboards.WN, "wn")
            emitPieces(bitboards.WP, "wp")
            // Black
            emitPieces(bitboards.BK, "bk")
            emitPieces(bitboards.BQ, "bq")
            emitPieces(bitboards.BR, "br")
            emitPieces(bitboards.BB, "bb")
            emitPieces(bitboards.BN, "bn")
            emitPieces(bitboards.BP, "bp")
        }
    }
}

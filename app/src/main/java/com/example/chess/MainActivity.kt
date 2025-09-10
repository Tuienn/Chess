// app/src/main/java/com/example/chess/MainActivity.kt
package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chess.pieces.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    ChessScreen()
                }
            }
        }
    }
}

/* ---------------------- UI tổng ---------------------- */

@Composable
fun ChessScreen() {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        ChessBoard(Modifier.matchParentSize())
        PiecesLayer(Modifier.matchParentSize())
    }
}

/* -------------------- Bàn cờ (Canvas) -------------------- */

@Composable
fun ChessBoard(
    modifier: Modifier = Modifier,
    lightSquare: Color = Color(0xFFEEEED2),
    darkSquare: Color = Color(0xFF769656)
) {
    Canvas(modifier) {
        val boardSize = size.minDimension
        val square = boardSize / 8f
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val isLight = (row + col) % 2 == 0
                drawRect(
                    color = if (isLight) lightSquare else darkSquare,
                    topLeft = Offset(col * square, row * square),
                    size = Size(square, square)
                )
            }
        }
    }
}

/* -------------------- Lớp quân cờ -------------------- */

/** Mô hình quân trên bàn (chỉ dùng cho render) */
data class Piece(val type: PieceType, val side: PieceSide)

/** Thế cờ ban đầu (4 hàng được yêu cầu) */
private fun initialBoard(): Array<Array<Piece?>> {
    val board = Array(8) { Array<Piece?>(8) { null } }

    // Hàng quân đen (row = 0)
    val blackBack = listOf(
        PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
        PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
    )
    for (c in 0..7) board[0][c] = Piece(blackBack[c], PieceSide.Black)

    // Hàng tốt đen (row = 1)
    for (c in 0..7) board[1][c] = Piece(PieceType.PAWN, PieceSide.Black)

    // Hàng tốt trắng (row = 6)
    for (c in 0..7) board[6][c] = Piece(PieceType.PAWN, PieceSide.White)

    // Hàng quân trắng (row = 7)
    val whiteBack = listOf(
        PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
        PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
    )
    for (c in 0..7) board[7][c] = Piece(whiteBack[c], PieceSide.White)

    return board
}

/** Render lớp quân cờ theo trạng thái board 8×8 */
@Composable
fun PiecesLayer(modifier: Modifier = Modifier, board: Array<Array<Piece?>> = initialBoard()) {
    Column(modifier) {
        for (row in 0 until 8) {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                for (col in 0 until 8) {
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        board[row][col]?.let { piece ->
                            when (piece.type) {
                                PieceType.PAWN   -> PawnPiece(Modifier.matchParentSize(), piece.side)
                                PieceType.ROOK   -> RookPiece(Modifier.matchParentSize(), piece.side)
                                PieceType.KNIGHT -> KnightPiece(Modifier.matchParentSize(), piece.side)
                                PieceType.BISHOP -> BishopPiece(Modifier.matchParentSize(), piece.side)
                                PieceType.QUEEN  -> QueenPiece(Modifier.matchParentSize(), piece.side)
                                PieceType.KING   -> KingPiece(Modifier.matchParentSize(), piece.side)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* -------------------- Preview (tuỳ chọn) -------------------- */

@Preview(showBackground = true)
@Composable
private fun PreviewChess() {
    MaterialTheme {
        ChessScreen()
    }
}

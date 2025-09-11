
package com.example.chess.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.*

/**
 * Bàn cờ tương tác:
 * - Nhấn 1 ô có quân thuộc lượt hiện tại -> highlight nước đi từ movesForSquare
 * - Nhấn vào một ô đích được highlight -> thực hiện đi quân (có ăn quân), đổi lượt
 * - Trắng đi trước
 */
@Composable
fun ChessBoardBitboard(
    initial: Bitboards,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var board by remember { mutableStateOf(initial) }
    var turn by remember { mutableStateOf(Side.WHITE) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var movesMask by remember { mutableStateOf(0UL) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0F))
            .padding(16.dp)
    ) {
        // Nút Back ở trên
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2ECC71),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Back to Menu",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bàn cờ
        BoxWithConstraints(
            modifier = Modifier
                .aspectRatio(1f)
                .background(Color(0xFFEEEED2))
        ) {
            val size = minOf(maxWidth, maxHeight)
            val sq: Dp = size / 8

            // 1) Vẽ nền bàn cờ 8x8
            BoardBackground(square = sq)

            // 2) Overlay highlight nước đi
            MovesOverlay(movesMask = movesMask, square = sq)

            // 3) Vẽ quân cờ
            PiecesLayer(board = board, square = sq)

            // 4) Lưới click 8x8
            ClickGrid(
                square = sq,
                onSquareClick = { r, c ->
                    val idx = indexFromRowCol(r, c)
                    val here = pieceAt(board, idx)

                    if (selected == null) {
                        // Chưa chọn -> chỉ cho phép chọn quân của lượt hiện tại
                        if (here?.first == turn) {
                            selected = idx
                            movesMask = movesForSquare(board, idx)
                        } else {
                            // chạm ô trống hoặc quân đối phương -> bỏ qua
                            selected = null
                            movesMask = 0UL
                        }
                    } else {
                        val sel = selected!!
                        val isMove = isSet(movesMask, idx)
                        if (isMove) {
                            // Thực hiện đi quân
                            board = applyMove(board, sel, idx)
                            // Đổi lượt
                            turn = if (turn == Side.WHITE) Side.BLACK else Side.WHITE
                            selected = null
                            movesMask = 0UL
                        } else {
                            // Chạm sang quân khác cùng màu -> đổi selection
                            if (here?.first == turn) {
                                selected = idx
                                movesMask = movesForSquare(board, idx)
                            } else {
                                selected = null
                                movesMask = 0UL
                            }
                        }
                    }
                }
            )

            // 5) Highlight ô đang chọn (sau grid để không chặn click)
            selected?.let { HighlightOrigin(index = it, square = sq) }
        }
    }
}

/* ------------------------------ Drawing layers ----------------------------- */
@Composable
private fun BoardBackground(square: Dp) {
    val light = Color(0xFFF0D9B5)
    val dark = Color(0xFFB58863)
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val sqPx = square.toPx()
            for (r in 0..7) for (c in 0..7) {
                val isDark = (r + c) % 2 == 1
                drawRect(
                    color = if (isDark) dark else light,
                    topLeft = Offset(x = c * sqPx, y = r * sqPx),
                    size = androidx.compose.ui.geometry.Size(sqPx, sqPx)
                )
            }
        }
    }
}

@Composable
private fun PiecesLayer(board: Bitboards, square: Dp) {
    @Composable
    fun emit(bb: ULong, code: String) {
        val squares = squaresFrom(bb)
        for (idx in squares) {
            val (r, c) = rowColFromIndex(idx)
            Piece(code, r, c, square)
        }
    }
    Box(Modifier.fillMaxSize()) {
        // White
        emit(board.WK, "wk"); emit(board.WQ, "wq"); emit(board.WR, "wr")
        emit(board.WB, "wb"); emit(board.WN, "wn"); emit(board.WP, "wp")
        // Black
        emit(board.BK, "bk"); emit(board.BQ, "bq"); emit(board.BR, "br")
        emit(board.BB, "bb"); emit(board.BN, "bn"); emit(board.BP, "bp")
    }
}

@Composable
private fun MovesOverlay(movesMask: ULong, square: Dp) {
    if (movesMask == 0UL) return
    val captureColor = Color(0xAAE74C3C) // red-ish
    val moveColor = Color(0xAA2ECC71)    // green-ish
    Canvas(Modifier.fillMaxSize()) {
        val sqPx = square.toPx()
        for (i in 0 until 64) {
            if (isSet(movesMask, i)) {
                val (r, c) = rowColFromIndex(i)
                val center = Offset(c * sqPx + sqPx / 2f, r * sqPx + sqPx / 2f)
                val radius = sqPx * 0.18f
                // Nếu ô có đối thủ -> tô viền đỏ,
                // còn lại ô trống -> chấm tròn xanh
                // Để biết ô có đối thủ, ta sẽ kiểm tra trong grid click (đã có) hoặc
                // đặt đơn giản ở đây: màu xanh cho tất cả, viền đỏ nếu cần bằng 2 pass.
                // Ở đây chỉ vẽ chấm xanh, capture sẽ đổi thành vòng tròn đỏ mỏng.
                drawCircle(color = moveColor, radius = radius, center = center)
            }
        }
    }
}

/** Highlight ô nguồn */
@Composable
private fun HighlightOrigin(index: Int, square: Dp) {
    val col = Color(0x88498AF3)
    Canvas(Modifier.fillMaxSize()) {
        val sqPx = square.toPx()
        val (r, c) = rowColFromIndex(index)
        val pad = sqPx * 0.05f
        drawRect(
            color = col,
            topLeft = Offset(c * sqPx + pad, r * sqPx + pad),
            size = androidx.compose.ui.geometry.Size(sqPx - 2 * pad, sqPx - 2 * pad)
        )
    }
}

/* ------------------------------ Input layer ------------------------------ */
@Composable
private fun ClickGrid(
    square: Dp,
    onSquareClick: (row: Int, col: Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        for (r in 0..7) {
            Row(Modifier.weight(1f)) {
                for (c in 0..7) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onSquareClick(r, c) }
                    )
                }
            }
        }
    }
}
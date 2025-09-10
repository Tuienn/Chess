package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ----------------------------- Bitboard model ----------------------------- */
// Chuẩn bitboard: bit 0 = a1, bit 7 = h1, bit 56 = a8, bit 63 = h8 (tăng trái→phải, dưới→trên)
data class Bitboards(
    val WP: ULong, val WN: ULong, val WB: ULong, val WR: ULong, val WQ: ULong, val WK: ULong,
    val BP: ULong, val BN: ULong, val BB: ULong, val BR: ULong, val BQ: ULong, val BK: ULong
)

private fun indexFromRowCol(row: Int, col: Int): Int = (7 - row) * 8 + col
private fun rowColFromIndex(index: Int): Pair<Int, Int> = (7 - index / 8) to (index % 8)

private fun bbOf(vararg idx: Int): ULong {
    var bb = 0UL
    for (i in idx) bb = bb or (1UL shl i)
    return bb
}

private fun squaresFrom(bb: ULong): List<Int> {
    if (bb == 0UL) return emptyList()
    val out = ArrayList<Int>(16)
    var x = bb
    var i = 0
    while (i < 64) {
        if (((x shr i) and 1UL) != 0UL) out.add(i)
        i++
    }
    return out
}

/* Vị trí khởi đầu chuẩn */
private fun initialBitboards(): Bitboards {
    // Pawns
    val wp = bbOf(*(0.until(8).map { indexFromRowCol(6, it) }.toIntArray()))
    val bp = bbOf(*(0.until(8).map { indexFromRowCol(1, it) }.toIntArray()))
    // Rooks
    val wr = bbOf(indexFromRowCol(7, 0), indexFromRowCol(7, 7))
    val br = bbOf(indexFromRowCol(0, 0), indexFromRowCol(0, 7))
    // Knights
    val wn = bbOf(indexFromRowCol(7, 1), indexFromRowCol(7, 6))
    val bn = bbOf(indexFromRowCol(0, 1), indexFromRowCol(0, 6))
    // Bishops
    val wb = bbOf(indexFromRowCol(7, 2), indexFromRowCol(7, 5))
    val bb = bbOf(indexFromRowCol(0, 2), indexFromRowCol(0, 5))
    // Queens
    val wq = bbOf(indexFromRowCol(7, 3))
    val bq = bbOf(indexFromRowCol(0, 3))
    // Kings
    val wk = bbOf(indexFromRowCol(7, 4))
    val bk = bbOf(indexFromRowCol(0, 4))

    return Bitboards(
        WP = wp, WN = wn, WB = wb, WR = wr, WQ = wq, WK = wk,
        BP = bp, BN = bn, BB = bb, BR = br, BQ = bq, BK = bk
    )
}

/* ----------------------------- Piece resources ---------------------------- */
private fun resForPiece(code: String): Int = when (code) {
    "wk" -> R.drawable.wk; "wq" -> R.drawable.wq; "wr" -> R.drawable.wr
    "wb" -> R.drawable.wb; "wn" -> R.drawable.wn; "wp" -> R.drawable.wp
    "bk" -> R.drawable.bk; "bq" -> R.drawable.bq; "br" -> R.drawable.br
    "bb" -> R.drawable.bb; "bn" -> R.drawable.bn; "bp" -> R.drawable.bp
    else -> error("Unknown piece code: $code")
}

/* ------------------------------- Composables ------------------------------ */
/** Composable Piece (tách riêng): vẽ 1 quân cờ ở (row,col) với kích thước 1 ô. */
@Composable
fun Piece(
    code: String,
    row: Int,
    col: Int,
    square: Dp,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    @DrawableRes val resId = resForPiece(code)
    Image(
        painter = painterResource(resId),
        contentDescription = code,
        contentScale = contentScale,
        modifier = modifier
            .size(square)
            .offset(x = square * col, y = square * row)
    )
}

/** Bàn cờ 8x8 chiếm toàn bộ chiều ngang + số (1–8) và chữ (a–h) như bạn yêu cầu. */
@Composable
fun ChessBoardCanvas(
    modifier: Modifier = Modifier,
    lightColor: Color = Color(0xFFEEEED2),
    darkColor: Color = Color(0xFF769656)
) {
    val tm = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val boardSize = minOf(size.width, size.height)
        val sq = boardSize / 8f
        val x0 = (size.width - boardSize) / 2f
        val y0 = (size.height - boardSize) / 2f
        val pad = 4.dp.toPx()

        val fontSizeSp = (sq * 0.28f / (density * fontScale)).sp
        val style = TextStyle(fontSize = fontSizeSp, fontWeight = FontWeight.SemiBold)

        // 64 ô
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val color = if ((r + c) % 2 == 0) lightColor else darkColor
                drawRect(color, Offset(x0 + c * sq, y0 + r * sq), Size(sq, sq))
            }
        }

        // Viền
        drawRect(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(x0, y0),
            size = Size(boardSize, boardSize),
            style = Stroke(width = 2.dp.toPx())
        )

        // Số 1–8 cột trái (góc trên-trái) — ô xanh chữ trắng, ô sáng chữ xanh
        for (r in 0 until 8) {
            val number = (8 - r).toString()
            val isDark = ((r + 0) % 2 != 0)
            val textColor = if (isDark) Color.White else darkColor
            drawText(
                textMeasurer = tm,
                text = number,
                topLeft = Offset(x0 + pad, y0 + r * sq + pad),
                style = style.copy(color = textColor)
            )
        }

        // Chữ a–h hàng dưới (góc dưới-phải)
        for (c in 0 until 8) {
            val letter = ('a'.code + c).toChar().toString()
            val r = 7
            val isDark = ((r + c) % 2 != 0)
            val textColor = if (isDark) Color.White else darkColor
            val layout = tm.measure(AnnotatedString(letter), style = style.copy(color = textColor))
            drawText(
                textMeasurer = tm,
                text = letter,
                topLeft = Offset(
                    x = x0 + c * sq + sq - pad - layout.size.width,
                    y = y0 + r * sq + sq - pad - layout.size.height
                ),
                style = style.copy(color = textColor)
            )
        }
    }
}

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

/* --------------------------------- Activity -------------------------------- */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val start = initialBitboards()

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ChessBoardBitboard(
                        bitboards = start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

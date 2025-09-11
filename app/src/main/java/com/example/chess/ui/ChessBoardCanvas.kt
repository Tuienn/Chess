package com.example.chess.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Bàn cờ 8x8 chiếm toàn bộ chiều ngang + số (1–8) và chữ (a–h) như bạn yêu cầu. */
@Composable
fun ChessBoardCanvas(
    modifier: Modifier = Modifier,
    lightColor: Color = Color(0xFFEEEED2),
    darkColor: Color = Color(0xFF769656)
) {
    val tm = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val boardSize = size.width  // Sử dụng toàn bộ chiều ngang
        val sq = boardSize / 8f
        val x0 = 0f  // Bắt đầu từ cạnh trái
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
                style = style.copy(color = textColor, fontSize = fontSizeSp * 0.7f)
            )
        }

        // Chữ a–h hàng dưới (góc dưới-phải)
        for (c in 0 until 8) {
            val letter = ('a'.code + c).toChar().toString()
            val r = 7
            val isDark = ((r + c) % 2 != 0)
            val textColor = if (isDark) Color.White else darkColor
            val layout = tm.measure(AnnotatedString(letter), style = style.copy(color = textColor, fontSize = fontSizeSp * 0.7f))
            drawText(
                textMeasurer = tm,
                text = letter,
                topLeft = Offset(
                    x = x0 + c * sq + sq - pad - layout.size.width,
                    y = y0 + r * sq + sq - pad - layout.size.height
                ),
                style = style.copy(color = textColor, fontSize = fontSizeSp * 0.7f)
            )
        }
    }
}

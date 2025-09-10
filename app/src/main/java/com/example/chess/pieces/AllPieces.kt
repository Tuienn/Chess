// com/example/chess/pieces/AllPieces.kt
package com.example.chess.pieces

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin


/* ---------- Tiện ích scale từ hệ 0..1 sang pixel ---------- */
private val DrawScope.w get() = size.width
private val DrawScope.h get() = size.height
private fun DrawScope.X(x: Float) = x * w
private fun DrawScope.Y(y: Float) = y * h
private fun DrawScope.S(s: Float) = s * minOf(w, h)

/* ---------- Helper vẽ path với fill + stroke ---------- */
private fun DrawScope.drawPiece(path: Path, c: PieceColors) {
    // tô
    drawPath(path = path, color = c.fill, style = Fill)
    // viền
    drawPath(
        path = path,
        color = c.stroke,
        style = Stroke(width = S(0.02f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/* =================== PAWN (TỐT) =================== */
@Composable
fun PawnPiece(modifier: Modifier, side: PieceSide) =
    PieceCanvas(modifier) { drawPawn(colorsFor(side)) }

private fun DrawScope.drawPawn(c: PieceColors) {
    val p = Path().apply {
        // đĩa chân
        addRoundRect(
            RoundRect(
                Rect(X(0.20f), Y(0.84f), X(0.80f), Y(0.92f)),
                S(0.02f), S(0.02f)
            )
        )
        // bệ chân
        addRoundRect(
            RoundRect(
                Rect(X(0.28f), Y(0.78f), X(0.72f), Y(0.84f)),
                S(0.02f), S(0.02f)
            )
        )
        // thân phình
        moveTo(X(0.35f), Y(0.78f))
        cubicTo(X(0.35f), Y(0.62f), X(0.42f), Y(0.56f), X(0.50f), Y(0.56f))
        cubicTo(X(0.58f), Y(0.56f), X(0.65f), Y(0.62f), X(0.65f), Y(0.78f))
        close()
        // đầu tròn
        addOval(Rect(X(0.40f), Y(0.28f), X(0.60f), Y(0.56f)))
    }
    drawPiece(p, c)
}

/* =================== ROOK (XE) =================== */
@Composable
fun RookPiece(modifier: Modifier, side: PieceSide) =
    PieceCanvas(modifier) { drawRook(colorsFor(side)) }

private fun DrawScope.drawRook(c: PieceColors) {
    val p = Path().apply {
        // đế
        addRoundRect(
            RoundRect(
                Rect(X(0.18f), Y(0.84f), X(0.82f), Y(0.92f)),
                S(0.02f), S(0.02f)
            )
        )
        // thân
        addRoundRect(
            RoundRect(
                Rect(X(0.30f), Y(0.38f), X(0.70f), Y(0.84f)),
                S(0.02f), S(0.02f)
            )
        )
        // răng lũy
        moveTo(X(0.28f), Y(0.38f))
        lineTo(X(0.72f), Y(0.38f))
        lineTo(X(0.72f), Y(0.26f))
        lineTo(X(0.60f), Y(0.26f))
        lineTo(X(0.60f), Y(0.32f))
        lineTo(X(0.52f), Y(0.32f))
        lineTo(X(0.52f), Y(0.26f))
        lineTo(X(0.48f), Y(0.26f))
        lineTo(X(0.48f), Y(0.32f))
        lineTo(X(0.40f), Y(0.32f))
        lineTo(X(0.40f), Y(0.26f))
        lineTo(X(0.28f), Y(0.26f))
        close()
    }
    drawPiece(p, c)
}

/* =================== BISHOP (TƯỢNG) =================== */
@Composable
fun BishopPiece(modifier: Modifier, side: PieceSide) =
    PieceCanvas(modifier) { drawBishop(colorsFor(side)) }

private fun DrawScope.drawBishop(c: PieceColors) {
    val p = Path().apply {
        // đế
        addRoundRect(
            RoundRect(
                Rect(X(0.20f), Y(0.84f), X(0.80f), Y(0.92f)),
                S(0.02f), S(0.02f)
            )
        )
        // thân cong
        moveTo(X(0.35f), Y(0.84f))
        cubicTo(X(0.35f), Y(0.64f), X(0.43f), Y(0.50f), X(0.50f), Y(0.42f))
        cubicTo(X(0.57f), Y(0.50f), X(0.65f), Y(0.64f), X(0.65f), Y(0.84f))
        close()
        // mũ bầu
        addOval(Rect(X(0.40f), Y(0.26f), X(0.60f), Y(0.46f)))
    }
    drawPiece(p, c)

    // khe mũ chéo
    drawLine(
        color = c.stroke,
        start = Offset(X(0.45f), Y(0.32f)),
        end = Offset(X(0.55f), Y(0.40f)),
        strokeWidth = S(0.018f)
    )
    // viên tròn đỉnh
    drawCircle(color = c.fill,  radius = S(0.028f), center = Offset(X(0.50f), Y(0.20f)))
    drawCircle(
        color = c.stroke,
        radius = S(0.028f),
        center = Offset(X(0.50f), Y(0.20f)),
        style = Stroke(width = S(0.012f))
    )
}

/* =================== KNIGHT (MÃ) =================== */
@Composable
fun KnightPiece(modifier: Modifier, side: PieceSide) =
    PieceCanvas(modifier) { drawKnight(colorsFor(side)) }

private fun DrawScope.drawKnight(c: PieceColors) {
    val p = Path().apply {
        // đế
        addRoundRect(
            RoundRect(
                Rect(X(0.18f), Y(0.84f), X(0.82f), Y(0.92f)),
                S(0.02f), S(0.02f)
            )
        )
        // đầu + cổ ngựa kiểu “Merida”
        moveTo(X(0.28f), Y(0.84f))
        cubicTo(X(0.33f), Y(0.70f), X(0.32f), Y(0.54f), X(0.45f), Y(0.46f))
        cubicTo(X(0.56f), Y(0.40f), X(0.66f), Y(0.28f), X(0.70f), Y(0.28f))
        cubicTo(X(0.78f), Y(0.28f), X(0.82f), Y(0.36f), X(0.76f), Y(0.42f))
        lineTo(X(0.79f), Y(0.48f)) // mõm
        lineTo(X(0.71f), Y(0.41f)) // tai trước
        lineTo(X(0.65f), Y(0.46f)) // giữa
        cubicTo(X(0.60f), Y(0.58f), X(0.58f), Y(0.68f), X(0.72f), Y(0.84f))
        close()
    }
    drawPiece(p, c)

    // mắt
    drawCircle(
        color = c.stroke,
        radius = S(0.018f),
        center = Offset(X(0.64f), Y(0.43f)),
        style = Stroke(width = S(0.010f))
    )
}

/* =================== QUEEN (HẬU) =================== */
@Composable
fun QueenPiece(modifier: Modifier, side: PieceSide) =
    PieceCanvas(modifier) { drawQueen(colorsFor(side)) }

private fun DrawScope.drawQueen(c: PieceColors) {
    val p = Path().apply {
        // đế
        addRoundRect(
            RoundRect(
                Rect(X(0.16f), Y(0.84f), X(0.84f), Y(0.92f)),
                S(0.02f), S(0.02f)
            )
        )
        // thân phình
        moveTo(X(0.30f), Y(0.84f))
        cubicTo(X(0.34f), Y(0.64f), X(0.40f), Y(0.46f), X(0.50f), Y(0.46f))
        cubicTo(X(0.60f), Y(0.46f), X(0.66f), Y(0.64f), X(0.70f), Y(0.84f))
        close()
        // vành vương miện
        addRoundRect(
            RoundRect(
                Rect(X(0.36f), Y(0.38f), X(0.64f), Y(0.46f)),
                S(0.02f), S(0.02f)
            )
        )
        // vương miện 5 chóp
        moveTo(X(0.36f), Y(0.38f))
        lineTo(X(0.32f), Y(0.26f))
        lineTo(X(0.40f), Y(0.30f))
        lineTo(X(0.46f), Y(0.18f))
        lineTo(X(0.50f), Y(0.26f))
        lineTo(X(0.54f), Y(0.18f))
        lineTo(X(0.60f), Y(0.30f))
        lineTo(X(0.68f), Y(0.26f))
        lineTo(X(0.64f), Y(0.38f))
        close()
    }
    drawPiece(p, c)

    // bi trên 5 chóp
    listOf(
        Offset(X(0.32f), Y(0.26f)),
        Offset(X(0.46f), Y(0.18f)),
        Offset(X(0.50f), Y(0.14f)),
        Offset(X(0.54f), Y(0.18f)),
        Offset(X(0.68f), Y(0.26f)),
    ).forEach { o ->
        drawCircle(color = c.fill,  radius = S(0.022f), center = o)
        drawCircle(color = c.stroke, radius = S(0.022f), center = o, style = Stroke(width = S(0.010f)))
    }
}

/* =================== KING (VUA) =================== */
@Composable
fun KingPiece(modifier: Modifier, side: PieceSide) =
    PieceCanvas(modifier) { drawKing(colorsFor(side)) }

private fun DrawScope.drawKing(c: PieceColors) {
    val p = Path().apply {
        // đế
        addRoundRect(
            RoundRect(
                Rect(X(0.16f), Y(0.84f), X(0.84f), Y(0.92f)),
                S(0.02f), S(0.02f)
            )
        )
        // thân
        moveTo(X(0.32f), Y(0.84f))
        cubicTo(X(0.36f), Y(0.62f), X(0.42f), Y(0.46f), X(0.50f), Y(0.46f))
        cubicTo(X(0.58f), Y(0.46f), X(0.64f), Y(0.62f), X(0.68f), Y(0.84f))
        close()

        // khối vuông vương miện
        addRoundRect(
            RoundRect(
                Rect(X(0.40f), Y(0.32f), X(0.60f), Y(0.44f)),
                S(0.015f), S(0.015f)
            )
        )
    }
    drawPiece(p, c)

    // thánh giá
    val t = S(0.012f)
    drawLine(
        color = c.stroke,
        start = Offset(X(0.50f), Y(0.12f)),
        end   = Offset(X(0.50f), Y(0.30f)),
        strokeWidth = t
    )
    drawLine(
        color = c.stroke,
        start = Offset(X(0.44f), Y(0.20f)),
        end   = Offset(X(0.56f), Y(0.20f)),
        strokeWidth = t
    )
}

/* =================== HÀM TIỆN DÙNG (liệt kê theo type) =================== */
@Composable
fun Piece(modifier: Modifier, type: PieceType, side: PieceSide) {
    when (type) {
        PieceType.PAWN   -> PawnPiece(modifier, side)
        PieceType.ROOK   -> RookPiece(modifier, side)
        PieceType.BISHOP -> BishopPiece(modifier, side)
        PieceType.KNIGHT -> KnightPiece(modifier, side)
        PieceType.QUEEN  -> QueenPiece(modifier, side)
        PieceType.KING   -> KingPiece(modifier, side)

    }
}

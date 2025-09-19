package com.example.chess.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chess.model.Side

/* ----------------------------- Piece resources ----------------------------
 * ĐÃ có sẵn trong dự án của bạn (để ở com.example.chess.ui):
 * fun resForPiece(code: String): Int = when (code) {
 *   "wk"->..., "wq"->..., "wr"->..., "wb"->..., "wn"->..., "wp"->...,
 *   "bk"->..., "bq"->..., "br"->..., "bb"->..., "bn"->..., "bp"->...
 * }
 * ------------------------------------------------------------------------- */

/** Map (side, pieceChar) -> code "wq","wr","wb","wn" hoặc "bq","br","bb","bn" */
private fun promotionCode(side: Side, ch: Char): String {
    val color = if (side == Side.WHITE) 'w' else 'b'
    val kind = when (ch.uppercaseChar()) {
        'Q' -> 'q'
        'R' -> 'r'
        'B' -> 'b'
        'N' -> 'n'
        else -> error("Unsupported promotion piece: $ch")
    }
    return "$color$kind"
}

/**
 * UI chọn quân phong cấp: Q / R / B / N
 * - side: để hiện đúng màu (trắng/đen)
 * - onPick: trả về 'Q','R','B','N'
 */
@Composable
fun PromotionPicker(
    side: Side,
    onPick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Chọn quân phong cấp",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val options = listOf('Q', 'R', 'B', 'N')
            for (ch in options) {
                val code = promotionCode(side, ch)
                val resId = resForPiece(code)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable { onPick(ch) }
                ) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Promote to $ch",
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        when (ch) {
                            'Q' -> "Hậu"
                            'R' -> "Xe"
                            'B' -> "Tượng"
                            else -> "Mã"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

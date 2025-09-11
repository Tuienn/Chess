package com.example.chess.ui

import com.example.chess.R

/* ----------------------------- Piece resources ---------------------------- */
fun resForPiece(code: String): Int = when (code) {
    "wk" -> R.drawable.wk; "wq" -> R.drawable.wq; "wr" -> R.drawable.wr
    "wb" -> R.drawable.wb; "wn" -> R.drawable.wn; "wp" -> R.drawable.wp
    "bk" -> R.drawable.bk; "bq" -> R.drawable.bq; "br" -> R.drawable.br
    "bb" -> R.drawable.bb; "bn" -> R.drawable.bn; "bp" -> R.drawable.bp
    else -> error("Unknown piece code: $code")
}


package com.example.chess.model

/* ---------------------------- Move generation ----------------------------- */
enum class Side { WHITE, BLACK }

data class Occupancy(val white: ULong, val black: ULong, val all: ULong)

private fun has(bb: ULong, i: Int): Boolean = isSet(bb, i)

fun occupancy(b: Bitboards): Occupancy {
    val w = b.WP or b.WN or b.WB or b.WR or b.WQ or b.WK
    val bl = b.BP or b.BN or b.BB or b.BR or b.BQ or b.BK
    return Occupancy(w, bl, w or bl)
}

/** Trả về (Side, Type) ở ô index nếu có. Type: 'K','Q','R','B','N','P' */
fun pieceAt(b: Bitboards, index: Int): Pair<Side, Char>? {
    val m = bitAt(index)
    return when {
        (b.WK and m) != 0UL -> Side.WHITE to 'K'
        (b.WQ and m) != 0UL -> Side.WHITE to 'Q'
        (b.WR and m) != 0UL -> Side.WHITE to 'R'
        (b.WB and m) != 0UL -> Side.WHITE to 'B'
        (b.WN and m) != 0UL -> Side.WHITE to 'N'
        (b.WP and m) != 0UL -> Side.WHITE to 'P'
        (b.BK and m) != 0UL -> Side.BLACK to 'K'
        (b.BQ and m) != 0UL -> Side.BLACK to 'Q'
        (b.BR and m) != 0UL -> Side.BLACK to 'R'
        (b.BB and m) != 0UL -> Side.BLACK to 'B'
        (b.BN and m) != 0UL -> Side.BLACK to 'N'
        (b.BP and m) != 0UL -> Side.BLACK to 'P'
        else -> null
    }
}

private fun onBoard(r: Int, c: Int) = r in 0..7 && c in 0..7

private fun kingMoves(from: Int, own: ULong): ULong {
    val (r0, c0) = rowColFromIndex(from)
    var moves = 0UL
    for (dr in -1..1) for (dc in -1..1) {
        if (dr == 0 && dc == 0) continue
        val r = r0 + dr; val c = c0 + dc
        if (!onBoard(r, c)) continue
        val i = indexFromRowCol(r, c)
        if (!has(own, i)) moves = moves or bitAt(i)
    }
    return moves
}

private fun knightMoves(from: Int, own: ULong): ULong {
    val (r0, c0) = rowColFromIndex(from)
    var moves = 0UL
    val deltas = arrayOf(
        -2 to -1, -2 to +1, -1 to -2, -1 to +2,
        +1 to -2, +1 to +2, +2 to -1, +2 to +1
    )
    for ((dr, dc) in deltas) {
        val r = r0 + dr; val c = c0 + dc
        if (!onBoard(r, c)) continue
        val i = indexFromRowCol(r, c)
        if (!has(own, i)) moves = moves or bitAt(i)
    }
    return moves
}

private fun slide(from: Int, own: ULong, opp: ULong, dirs: Array<Pair<Int, Int>>): ULong {
    val (r0, c0) = rowColFromIndex(from)
    var res = 0UL
    for ((dr, dc) in dirs) {
        var r = r0; var c = c0
        while (true) {
            r += dr; c += dc
            if (!onBoard(r, c)) break
            val i = indexFromRowCol(r, c)
            if (has(own, i)) break
            res = res or bitAt(i)
            if (has(opp, i)) break
        }
    }
    return res
}

private fun rookMoves(from: Int, own: ULong, opp: ULong): ULong =
    slide(from, own, opp, arrayOf(0 to +1, 0 to -1, +1 to 0, -1 to 0))

private fun bishopMoves(from: Int, own: ULong, opp: ULong): ULong =
    slide(from, own, opp, arrayOf(+1 to +1, +1 to -1, -1 to +1, -1 to -1))

private fun queenMoves(from: Int, own: ULong, opp: ULong): ULong =
    rookMoves(from, own, opp) or bishopMoves(from, own, opp)

/** Pawn: đi thẳng 1 ô; ăn chéo 1 ô. Không 2 ô, không en passant, không phong cấp. */
private fun pawnMoves(from: Int, side: Side, occ: Occupancy): ULong {
    val (r0, c0) = rowColFromIndex(from)
    var res = 0UL
    when (side) {
        Side.WHITE -> {
            val rf = r0 - 1
            if (rf >= 0) {
                val iF = indexFromRowCol(rf, c0)
                if (!isSet(occ.all, iF)) res = res or bitAt(iF)
            }
            for ((r, c) in arrayOf(r0 - 1 to c0 - 1, r0 - 1 to c0 + 1)) {
                if (!onBoard(r, c)) continue
                val i = indexFromRowCol(r, c)
                if (isSet(occ.black, i)) res = res or bitAt(i)
            }
        }
        Side.BLACK -> {
            val rf = r0 + 1
            if (rf <= 7) {
                val iF = indexFromRowCol(rf, c0)
                if (!isSet(occ.all, iF)) res = res or bitAt(iF)
            }
            for ((r, c) in arrayOf(r0 + 1 to c0 - 1, r0 + 1 to c0 + 1)) {
                if (!onBoard(r, c)) continue
                val i = indexFromRowCol(r, c)
                if (isSet(occ.white, i)) res = res or bitAt(i)
            }
        }
    }
    return res
}

/** API chính: bitboard ô đích (pseudo-legal) cho ô fromIndex. */
fun movesForSquare(b: Bitboards, fromIndex: Int): ULong {
    val occ = occupancy(b)
    val p = pieceAt(b, fromIndex) ?: return 0UL
    val (side, type) = p
    val own = if (side == Side.WHITE) occ.white else occ.black
    val opp = if (side == Side.WHITE) occ.black else occ.white
    return when (type) {
        'K' -> kingMoves(fromIndex, own)
        'Q' -> queenMoves(fromIndex, own, opp)
        'R' -> rookMoves(fromIndex, own, opp)
        'B' -> bishopMoves(fromIndex, own, opp)
        'N' -> knightMoves(fromIndex, own)
        'P' -> pawnMoves(fromIndex, side, occ)
        else -> 0UL
    }
}

/* ---------------------------- Move application ---------------------------- */
/** Áp dụng nước đi from->to (bắt quân nếu có). Không xử lý đặc biệt. */
fun applyMove(b: Bitboards, fromIndex: Int, toIndex: Int): Bitboards {
    val who = pieceAt(b, fromIndex) ?: return b
    val (side, type) = who

    // Xóa quân bị bắt (nếu có) ở ô đích trước
    fun removeAt(bb: ULong): ULong = clearAt(bb, toIndex)
    val rWP = removeAt(b.WP); val rWN = removeAt(b.WN); val rWB = removeAt(b.WB)
    val rWR = removeAt(b.WR); val rWQ = removeAt(b.WQ); val rWK = removeAt(b.WK)
    val rBP = removeAt(b.BP); val rBN = removeAt(b.BN); val rBB = removeAt(b.BB)
    val rBR = removeAt(b.BR); val rBQ = removeAt(b.BQ); val rBK = removeAt(b.BK)

    // Di chuyển quân
    fun move(bb: ULong): ULong = setAt(clearAt(bb, fromIndex), toIndex)

    return when (side) {
        Side.WHITE -> when (type) {
            'P' -> b.copy(WP = move(rWP), WN = rWN, WB = rWB, WR = rWR, WQ = rWQ, WK = rWK,
                BP = rBP, BN = rBN, BB = rBB, BR = rBR, BQ = rBQ, BK = rBK)
            'N' -> b.copy(WP = rWP, WN = move(rWN), WB = rWB, WR = rWR, WQ = rWQ, WK = rWK,
                BP = rBP, BN = rBN, BB = rBB, BR = rBR, BQ = rBQ, BK = rBK)
            'B' -> b.copy(WP = rWP, WN = rWN, WB = move(rWB), WR = rWR, WQ = rWQ, WK = rWK,
                BP = rBP, BN = rBN, BB = rBB, BR = rBR, BQ = rBQ, BK = rBK)
            'R' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = move(rWR), WQ = rWQ, WK = rWK,
                BP = rBP, BN = rBN, BB = rBB, BR = rBR, BQ = rBQ, BK = rBK)
            'Q' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = rWR, WQ = move(rWQ), WK = rWK,
                BP = rBP, BN = rBN, BB = rBB, BR = rBR, BQ = rBQ, BK = rBK)
            'K' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = rWR, WQ = rWQ, WK = move(rWK),
                BP = rBP, BN = rBN, BB = rBB, BR = rBR, BQ = rBQ, BK = rBK)
            else -> b
        }
        Side.BLACK -> when (type) {
            'P' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = rWR, WQ = rWQ, WK = rWK,
                BP = move(rBP), BN = rBN, BB = rBB, BR = rBR, BQ = rBQ, BK = rBK)
            'N' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = rWR, WQ = rWQ, WK = rWK,
                BP = rBP, BN = move(rBN), BB = rBB, BR = rBR, BQ = rBQ, BK = rBK)
            'B' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = rWR, WQ = rWQ, WK = rWK,
                BP = rBP, BN = rBN, BB = move(rBB), BR = rBR, BQ = rBQ, BK = rBK)
            'R' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = rWR, WQ = rWQ, WK = rWK,
                BP = rBP, BN = rBN, BB = rBB, BR = move(rBR), BQ = rBQ, BK = rBK)
            'Q' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = rWR, WQ = rWQ, WK = rWK,
                BP = rBP, BN = rBN, BB = rBB, BR = rBR, BQ = move(rBQ), BK = rBK)
            'K' -> b.copy(WP = rWP, WN = rWN, WB = rWB, WR = rWR, WQ = rWQ, WK = rWK,
                BP = rBP, BN = rBN, BB = rBB, BR = rBR, BQ = rBQ, BK = move(rBK))
            else -> b
        }
    }
}
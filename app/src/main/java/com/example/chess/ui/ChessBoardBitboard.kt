
package com.example.chess.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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

/** Kiểm tra nước đi có phải tốt lên hàng cuối không */
private fun needsPromotionForMove(state: GameState, move: Move): Boolean {
    val who = pieceAt(state.boards, move.from) ?: return false
    val (side, type) = who
    if (type != 'P') return false
    val (rTo, _) = rowColFromIndex(move.to)
    return (side == Side.WHITE && rTo == 0) || (side == Side.BLACK && rTo == 7)
}


/**
 * Bàn cờ tương tác:
 * - Nhấn 1 ô có quân thuộc lượt hiện tại -> highlight nước đi từ movesForSquare
 * - Nhấn vào một ô đích được highlight -> thực hiện đi quân (có ăn quân), đổi lượt
 * - Trắng đi trước
 * - Hỗ trợ phong cấp tốt với PromotionPicker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessBoardBitboard(
    initial: Bitboards,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { 
        mutableStateOf(GameState(boards = initial, sideToMove = Side.WHITE)) 
    }
    ChessBoardBitboardImpl(gameState, onBack, modifier) { gameState = it }
}

/**
 * Overload để nhận GameState trực tiếp (cho testing)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessBoardBitboard(
    initialState: GameState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(initialState) }
    ChessBoardBitboardImpl(gameState, onBack, modifier) { gameState = it }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChessBoardBitboardImpl(
    gameState: GameState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onGameStateChange: (GameState) -> Unit
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    var availableMoves by remember { mutableStateOf<List<Move>>(emptyList()) }
    
    // Animation state cho quân cờ đang di chuyển
    var animatingPiece by remember { mutableStateOf<Triple<String, Int, Int>?>(null) } // (pieceCode, fromIdx, toIdx)
    var pendingMove by remember { mutableStateOf<Move?>(null) } // Move chờ thực hiện
    
    // Promotion state
    var pendingPromotionMove by remember { mutableStateOf<Move?>(null) } // Move cần phong cấp
    var showPromotionSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val animationProgress = animateFloatAsState(
        targetValue = if (animatingPiece != null) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        finishedListener = { 
            if (animatingPiece != null) {
                animatingPiece = null
            }
        }
    )
    
    // Handle pending move after animation
    LaunchedEffect(pendingMove) {
        pendingMove?.let { move ->
            kotlinx.coroutines.delay(300) // Đợi animation hoàn thành
            
            // Kiểm tra nếu cần phong cấp
            if (needsPromotionForMove(gameState, move)) {
                pendingPromotionMove = move
                showPromotionSheet = true
                pendingMove = null
            } else {
                // Thực hiện đi quân với GameState
                onGameStateChange(applyMove(gameState, move))
                pendingMove = null
            }
        }
    }
    
    // Function to apply promotion move
    val applyPromotionMove: (Char) -> Unit = { promotionPiece ->
        pendingPromotionMove?.let { move ->
            // Apply move with promotion
            val promotionMove = move.copy(promo = promotionPiece)
            onGameStateChange(applyMove(gameState, promotionMove))
            // Clear promotion state
            pendingPromotionMove = null
            showPromotionSheet = false
        }
    }

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
            MovesOverlay(moves = availableMoves, square = sq)

            // 3) Vẽ quân cờ
            PiecesLayer(
                board = gameState.boards, 
                square = sq,
                animatingPiece = animatingPiece,
                animationProgress = animationProgress.value
            )

            // 4) Lưới click 8x8
            ClickGrid(
                square = sq,
                onSquareClick = { r, c ->
                    val idx = indexFromRowCol(r, c)
                    val here = pieceAt(gameState.boards, idx)

                    if (selected == null) {
                        // Chưa chọn -> chỉ cho phép chọn quân của lượt hiện tại
                        if (here?.first == gameState.sideToMove) {
                            selected = idx
                            availableMoves = generateMoves(gameState, idx)
                        } else {
                            // chạm ô trống hoặc quân đối phương -> bỏ qua
                            selected = null
                            availableMoves = emptyList()
                        }
                    } else {
                        val sel = selected!!
                        // Tìm move tương ứng
                        val targetMove = availableMoves.find { it.from == sel && it.to == idx }
                        
                        if (targetMove != null) {
                            // Lấy thông tin quân cờ trước khi di chuyển
                            val movingPiece = pieceAt(gameState.boards, sel)
                            if (movingPiece != null) {
                                val sideChar = if (movingPiece.first == Side.WHITE) "w" else "b"
                                val pieceChar = movingPiece.second.lowercase()
                                val pieceCode = "$sideChar$pieceChar"
                                // Bắt đầu animation
                                animatingPiece = Triple(pieceCode, sel, idx)
                                // Set pending move để xử lý sau khi animation hoàn thành
                                pendingMove = targetMove
                            } else {
                                // Fallback nếu không tìm thấy quân cờ
                                onGameStateChange(applyMove(gameState, targetMove))
                            }
                            selected = null
                            availableMoves = emptyList()
                        } else {
                            // Chạm sang quân khác cùng màu -> đổi selection
                            if (here?.first == gameState.sideToMove) {
                                selected = idx
                                availableMoves = generateMoves(gameState, idx)
                            } else {
                                selected = null
                                availableMoves = emptyList()
                            }
                        }
                    }
                }
            )

            // 5) Highlight ô đang chọn (sau grid để không chặn click)
            selected?.let { HighlightOrigin(index = it, square = sq) }
        }
        
        // 6) Promotion Sheet
        if (showPromotionSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showPromotionSheet = false
                    pendingPromotionMove = null
                },
                sheetState = sheetState
            ) {
                PromotionPicker(
                    side = gameState.sideToMove, // Sử dụng lượt hiện tại từ GameState
                    onPick = applyPromotionMove
                )
            }
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
private fun PiecesLayer(
    board: Bitboards, 
    square: Dp, 
    animatingPiece: Triple<String, Int, Int>?, 
    animationProgress: Float
) {
    @Composable
    fun emit(bb: ULong, code: String) {
        val squares = squaresFrom(bb)
        for (idx in squares) {
            // Không hiển thị quân cờ đang di chuyển ở vị trí cũ
            if (animatingPiece != null && idx == animatingPiece.second && animatingPiece.first == code) {
                continue
            }
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
        
        // Render quân cờ đang di chuyển với animation
        animatingPiece?.let { (pieceCode, fromIdx, toIdx) ->
            val (fromR, fromC) = rowColFromIndex(fromIdx)
            val (toR, toC) = rowColFromIndex(toIdx)
            
            // Tính toán vị trí interpolated
            val currentR = fromR + (toR - fromR) * animationProgress
            val currentC = fromC + (toC - fromC) * animationProgress
            
            AnimatedPiece(
                code = pieceCode,
                row = currentR,
                col = currentC,
                square = square
            )
        }
    }
}

@Composable
private fun MovesOverlay(moves: List<Move>, square: Dp) {
    if (moves.isEmpty()) return
    val captureColor = Color(0xAAE74C3C) // red-ish
    val moveColor = Color(0xAA2ECC71)    // green-ish
    val castleColor = Color(0xAA3498DB)  // blue-ish for castling
    val enPassantColor = Color(0xAAF39C12) // orange-ish for en passant
    
    Canvas(Modifier.fillMaxSize()) {
        val sqPx = square.toPx()
        for (move in moves) {
            val (r, c) = rowColFromIndex(move.to)
            val center = Offset(c * sqPx + sqPx / 2f, r * sqPx + sqPx / 2f)
            val radius = sqPx * 0.18f
            
            // Chọn màu dựa trên loại nước đi
            val color = when {
                move.isCastle -> castleColor
                move.isEnPassant -> enPassantColor
                else -> moveColor // Bao gồm cả capture thông thường
            }
            
            drawCircle(color = color, radius = radius, center = center)
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

@Composable
private fun AnimatedPiece(
    code: String,
    row: Float,
    col: Float,
    square: Dp
) {
    Piece(
        code = code,
        row = row.toInt(), // Piece hiện tại chỉ nhận Int, nhưng offset sẽ được điều chỉnh
        col = col.toInt(),
        square = square,
        modifier = Modifier.offset(
            x = square * (col - col.toInt()), // Offset fractional part
            y = square * (row - row.toInt())
        )
    )
}
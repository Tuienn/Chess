package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.example.chess.R

@Composable
fun WatchGameScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0F))
            .padding(20.dp)
    ) {
        val context = LocalContext.current

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header đơn giản
            Text(
                text = "Live Preview",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // GIF giữa màn hình
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.drawable.game) // đặt game.gif ở drawable-nodpi => R.drawable.game
                    .decoderFactory(GifDecoder.Factory()) // Bắt buộc để decode GIF
                    .crossfade(true)
                    .build(),
                contentDescription = "Chess gameplay",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                contentScale = ContentScale.FillWidth
            )

            // Nút Back
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2ECC71),
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Back", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

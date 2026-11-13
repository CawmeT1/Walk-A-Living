package com.example.walk_a_living.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BarcodeView(
    code: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            val cleanCode = "*${code.replace("-", "")}*"
            var totalWidthUnits = 6f
            val bars = mutableListOf<Pair<Float, Float>>() // Pair(x, width)

            for (ch in cleanCode) {
                val v = ch.code
                for (i in 0 until 7) {
                    val w = 1f + ((v shr i) and 1) + (if (i % 3 == 0) 1f else 0f)
                    if (i % 2 == 0) {
                        bars.add(Pair(totalWidthUnits, w))
                    }
                    totalWidthUnits += w + 1f
                }
                totalWidthUnits += 2f
            }
            totalWidthUnits += 6f

            val scaleX = size.width / totalWidthUnits

            bars.forEach { (x, w) ->
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x * scaleX, 0f),
                    size = Size(w * scaleX, size.height)
                )
            }
        }
    }
}

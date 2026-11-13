package com.example.walk_a_living.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walk_a_living.ui.theme.LocalExtraColors

@Composable
fun TicketCard(
    title: String,
    subtitle: String,
    cost: Int? = null,
    code: String? = null,
    isOpen: Boolean = false,
    isOff: Boolean = false,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null,
    buttonEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val ticketBg = LocalExtraColors.current.ticket
    val bgParent = MaterialTheme.colorScheme.background
    val inkColor = Color(0xFF1B2430)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isOff) ticketBg.copy(alpha = 0.5f) else ticketBg)
            .alpha(if (isOff) 0.65f else 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = inkColor
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = inkColor.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (code != null) {
                    Text(
                        text = code,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = inkColor
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (isOpen && code != null && !isOff) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BarcodeView(code = code)
                }
            }

            // Dashed line & cutouts container canvas
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
            ) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = inkColor.copy(alpha = 0.45f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect
                )
            }

            // Stub content
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (cost != null) {
                    Text(
                        text = "$cost",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = inkColor,
                            lineHeight = 24.sp
                        )
                    )
                    Text(
                        text = "points",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = inkColor.copy(alpha = 0.8f)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (buttonText != null && onButtonClick != null) {
                    Button(
                        onClick = onButtonClick,
                        enabled = buttonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = inkColor,
                            contentColor = Color.White,
                            disabledContainerColor = inkColor.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                if (secondaryButtonText != null && onSecondaryButtonClick != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onSecondaryButtonClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = inkColor.copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = secondaryButtonText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }

        // Semi-circle cutout at top & bottom on stub border
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            val stubWidth = 110.dp.toPx()
            val cutoutRadius = 10.dp.toPx()
            val cutoutX = size.width - stubWidth

            // Top cutout
            drawCircle(
                color = bgParent,
                radius = cutoutRadius,
                center = Offset(cutoutX, 0f)
            )

            // Bottom cutout
            drawCircle(
                color = bgParent,
                radius = cutoutRadius,
                center = Offset(cutoutX, size.height)
            )
        }
    }
}

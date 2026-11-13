package com.example.walk_a_living.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.walk_a_living.data.Coupon
import com.example.walk_a_living.data.CouponVerificationResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MerchantScannerView(
    customerCoupons: List<Coupon>,
    verificationResult: CouponVerificationResult?,
    onScanCode: (String) -> Unit,
    onConfirmRedemption: (String) -> Unit,
    onClearResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var manualCodeInput by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }

    // Scanner animation laser position (0.0f to 1.0f)
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Camera Viewfinder Box
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Viewfinder Background & Laser Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cornerLength = 36.dp.toPx()
                    val strokeWidth = 4.dp.toPx()
                    val frameSize = size.height * 0.72f
                    val left = (size.width - frameSize) / 2f
                    val top = (size.height - frameSize) / 2f
                    val right = left + frameSize
                    val bottom = top + frameSize

                    val cornerColor = Color(0xFF10B981)

                    // Top-Left corner
                    drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
                    drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

                    // Top-Right corner
                    drawLine(cornerColor, Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
                    drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)

                    // Bottom-Left corner
                    drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
                    drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom + cornerLength), strokeWidth)

                    // Bottom-Right corner
                    drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
                    drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)

                    // Laser line
                    val laserY = top + (frameSize * laserPosition)
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(left + 8.dp.toPx(), laserY),
                        end = Offset(right - 8.dp.toPx(), laserY),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Overlay Controls & Instructions
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📷 CAMERA SCANNER",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF10B981)
                        )

                        // Flash toggle / Camera permission button
                        if (!hasCameraPermission) {
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Grant Camera Permission", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (flashEnabled) Color(0xFFF59E0B) else Color(0xFF334155))
                                    .clickable { flashEnabled = !flashEnabled }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (flashEnabled) "FLASH ON" else "FLASH OFF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = "Point camera at customer barcode to scan automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Barcode Entry Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = manualCodeInput,
                onValueChange = { manualCodeInput = it },
                placeholder = { Text("Scan or enter code (e.g. IC-8K2P-9M4Q)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (manualCodeInput.isNotBlank()) {
                            onScanCode(manualCodeInput.trim())
                        }
                    }
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (manualCodeInput.isNotBlank()) {
                        onScanCode(manualCodeInput.trim())
                    }
                },
                enabled = manualCodeInput.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Verify Code")
            }
        }

        // Quick Scan Customer Barcodes (Simulator / Testing Bench)
        if (customerCoupons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Simulated Scan - Customer Coupons (${customerCoupons.size}):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(customerCoupons, key = { it.id }) { coupon ->
                    val isUsed = coupon.used
                    val isExpired = coupon.exp < System.currentTimeMillis()

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUsed || isExpired) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        modifier = Modifier
                            .width(200.dp)
                            .clickable {
                                manualCodeInput = coupon.code
                                onScanCode(coupon.code)
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = coupon.code,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when {
                                        isUsed -> "Used"
                                        isExpired -> "Expired"
                                        else -> "Active"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        isUsed -> Color.Gray
                                        isExpired -> Color.Red
                                        else -> Color(0xFF10B981)
                                    }
                                )
                            }
                            Text(
                                text = coupon.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    manualCodeInput = coupon.code
                                    onScanCode(coupon.code)
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Scan Barcode", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Live Scan & Verification Result Card
        if (verificationResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            VerificationResultCard(
                result = verificationResult,
                onConfirmRedemption = { code ->
                    onConfirmRedemption(code)
                },
                onDismiss = onClearResult
            )
        }
    }
}

@Composable
private fun VerificationResultCard(
    result: CouponVerificationResult,
    onConfirmRedemption: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is CouponVerificationResult.Valid -> Color(0xFFECFDF5)
                is CouponVerificationResult.AlreadyUsed -> Color(0xFFFFFBEB)
                is CouponVerificationResult.Expired -> Color(0xFFFEF2F2)
                is CouponVerificationResult.NotFound -> Color(0xFFFEF2F2)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = when (result) {
                    is CouponVerificationResult.Valid -> Color(0xFF10B981)
                    is CouponVerificationResult.AlreadyUsed -> Color(0xFFF59E0B)
                    is CouponVerificationResult.Expired -> Color(0xFFEF4444)
                    is CouponVerificationResult.NotFound -> Color(0xFFEF4444)
                },
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadgeIcon(result = result)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (result) {
                            is CouponVerificationResult.Valid -> "VALID CUSTOMER COUPON"
                            is CouponVerificationResult.AlreadyUsed -> "ALREADY REDEEMED"
                            is CouponVerificationResult.Expired -> "EXPIRED COUPON"
                            is CouponVerificationResult.NotFound -> "INVALID CODE"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (result) {
                            is CouponVerificationResult.Valid -> Color(0xFF065F46)
                            is CouponVerificationResult.AlreadyUsed -> Color(0xFF92400E)
                            is CouponVerificationResult.Expired, is CouponVerificationResult.NotFound -> Color(0xFF991B1B)
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onDismiss() }
                        .padding(6.dp)
                ) {
                    Text("✕", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (result) {
                is CouponVerificationResult.Valid -> {
                    val coupon = result.coupon
                    Text(
                        text = coupon.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = "Issued by: ${coupon.by} · Value: ${coupon.cost} pts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF047857)
                    )
                    Text(
                        text = "Scanned Code: ${coupon.code}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = "Expires: ${dateFormat.format(Date(coupon.exp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF047857)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onConfirmRedemption(coupon.code) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Confirm Customer Redemption",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                is CouponVerificationResult.AlreadyUsed -> {
                    val coupon = result.coupon
                    Text(
                        text = coupon.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Code: ${coupon.code}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB45309)
                    )
                    Text(
                        text = "Redeemed at: ${coupon.usedAt?.let { dateFormat.format(Date(it)) } ?: "Previously"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB45309)
                    )
                }

                is CouponVerificationResult.Expired -> {
                    val coupon = result.coupon
                    Text(
                        text = coupon.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = "Code: ${coupon.code}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB91C1C)
                    )
                    Text(
                        text = "Expired on: ${dateFormat.format(Date(coupon.exp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB91C1C)
                    )
                }

                is CouponVerificationResult.NotFound -> {
                    Text(
                        text = "No active customer coupon found matching code \"${result.code}\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = "Please verify code spelling or ask customer to present their active barcode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB91C1C)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadgeIcon(result: CouponVerificationResult) {
    val (symbol, color) = when (result) {
        is CouponVerificationResult.Valid -> "✓" to Color(0xFF059669)
        is CouponVerificationResult.AlreadyUsed -> "!" to Color(0xFFD97706)
        is CouponVerificationResult.Expired, is CouponVerificationResult.NotFound -> "✕" to Color(0xFFDC2626)
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

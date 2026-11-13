package com.example.walk_a_living.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.walk_a_living.data.AppState
import com.example.walk_a_living.ui.WalkViewModel
import com.example.walk_a_living.ui.components.TicketCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    appState: AppState,
    viewModel: WalkViewModel,
    modifier: Modifier = Modifier
) {
    val coupons = appState.coupons
    val now = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Your coupons",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (coupons.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No coupons yet. Walk, earn points, then redeem one from Rewards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(28.dp)
                )
            }
        } else {
            coupons.forEach { coupon ->
                val isExpired = coupon.exp < now
                val isUsed = coupon.used
                val isDead = isUsed || isExpired

                val statusText = when {
                    isUsed -> "Used"
                    isExpired -> "Expired"
                    else -> "Expires ${dateFormat.format(Date(coupon.exp))}"
                }

                TicketCard(
                    title = coupon.name,
                    subtitle = "${coupon.by} · $statusText",
                    code = coupon.code,
                    isOpen = coupon.open && !isDead,
                    isOff = isDead,
                    buttonText = if (!isDead) (if (coupon.open) "Hide code" else "Show code") else null,
                    onButtonClick = if (!isDead) { { viewModel.toggleCouponOpen(coupon.id) } } else null,
                    secondaryButtonText = if (!isDead) "Mark used" else null,
                    onSecondaryButtonClick = if (!isDead) { { viewModel.markCouponUsed(coupon.id) } } else null,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

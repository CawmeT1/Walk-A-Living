package com.example.walk_a_living.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walk_a_living.data.AppState
import com.example.walk_a_living.ui.WalkViewModel
import com.example.walk_a_living.ui.components.TicketCard
import com.example.walk_a_living.ui.theme.LocalExtraColors
import java.util.Locale

@Composable
fun RewardsScreen(
    appState: AppState,
    viewModel: WalkViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Give Back Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Give back",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Donate points so students without data can get free Wi-Fi hours. The café is asked to turn every 100 donated points into 1 free hour for a student in need. To be confirmed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                val totalGiven = appState.give
                val pct = (totalGiven.toFloat() / WalkViewModel.GOAL_PTS.toFloat()).coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = LocalExtraColors.current.line
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${String.format(Locale.US, "%,d", totalGiven)} of ${String.format(Locale.US, "%,d", WalkViewModel.GOAL_PTS)} points given by you (${totalGiven / 100} free Wi-Fi hours at 100 points each)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(25, 50, 100).forEach { amount ->
                        val enabled = appState.balance >= amount
                        OutlinedButton(
                            onClick = { viewModel.givePoints(amount) },
                            enabled = enabled,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Give $amount")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Catalog Section
        Text(
            text = "Spend your points",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        val activeOffers = appState.offers.filter { it.active }

        activeOffers.forEach { offer ->
            val canAfford = appState.balance >= offer.cost
            val buttonText = if (canAfford) "Redeem" else "Need ${offer.cost - appState.balance}"

            TicketCard(
                title = offer.name,
                subtitle = "${offer.by} · valid ${WalkViewModel.VALID_DAYS} days",
                cost = offer.cost,
                buttonText = buttonText,
                buttonEnabled = canAfford,
                onButtonClick = { viewModel.redeemOffer(offer.id) },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

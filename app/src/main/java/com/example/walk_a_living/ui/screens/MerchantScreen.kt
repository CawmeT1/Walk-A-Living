package com.example.walk_a_living.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.walk_a_living.data.AppState
import com.example.walk_a_living.data.CouponVerificationResult
import com.example.walk_a_living.ui.WalkViewModel
import com.example.walk_a_living.ui.components.MerchantScannerView
import com.example.walk_a_living.ui.theme.LocalExtraColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MerchantScreen(
    appState: AppState,
    viewModel: WalkViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Scan & Verify", "Manage Offers", "Analytics & Log")

    var currentVerificationResult by remember { mutableStateOf<CouponVerificationResult?>(null) }

    var offerNameInput by remember { mutableStateOf("") }
    var offerBusinessInput by remember { mutableStateOf("") }
    var offerCostInput by remember { mutableStateOf("") }

    val coupons = appState.coupons
    val redeemedCoupons = coupons.filter { it.used }.sortedByDescending { it.usedAt ?: 0L }
    val pointsSpent = coupons.sumOf { it.cost }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Merchant Sub-Navigation Tabs
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            subTabs.forEachIndexed { index, title ->
                val isSelected = selectedSubTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedSubTab) {
            0 -> {
                // TAB 0: Scan & Verify Customer Codes
                MerchantScannerView(
                    customerCoupons = coupons,
                    verificationResult = currentVerificationResult,
                    onScanCode = { code ->
                        currentVerificationResult = viewModel.verifyCouponCode(code)
                    },
                    onConfirmRedemption = { code ->
                        viewModel.confirmRedeemCoupon(code)
                        currentVerificationResult = viewModel.verifyCouponCode(code)
                    },
                    onClearResult = {
                        currentVerificationResult = null
                    }
                )
            }

            1 -> {
                // TAB 1: Manage Offers Catalog
                Text(
                    text = "Live Merchant Offers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Offers listed here will immediately appear in the Customer's Rewards tab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (appState.offers.isEmpty()) {
                    Text(
                        text = "No offers created yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    appState.offers.forEach { offer ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = offer.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${offer.by} · ${offer.cost} pts · Prefix: ${offer.pre} · ${if (offer.active) "Active (Visible to Customers)" else "Paused"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.toggleOfferActive(offer.id) }
                                ) {
                                    Text(if (offer.active) "Pause" else "Resume")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create New Offer Form
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Add a new merchant offer",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = offerNameInput,
                            onValueChange = { offerNameInput = it },
                            placeholder = { Text("Offer description (e.g. 1 Free Coffee)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = offerBusinessInput,
                            onValueChange = { offerBusinessInput = it },
                            placeholder = { Text("Business / Store Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = offerCostInput,
                                onValueChange = { offerCostInput = it },
                                placeholder = { Text("Cost in customer points") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val cost = offerCostInput.toIntOrNull() ?: 0
                                    viewModel.addOffer(offerNameInput, offerBusinessInput, cost)
                                    offerNameInput = ""
                                    offerBusinessInput = ""
                                    offerCostInput = ""
                                }
                            ) {
                                Text("Publish Offer")
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: Redemptions Log & Performance Analytics
                Text(
                    text = "Merchant Overview & Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(value = "${coupons.size}", label = "Total Issued", modifier = Modifier.weight(1f))
                    StatCard(value = "${redeemedCoupons.size}", label = "Scanned / Redeemed", modifier = Modifier.weight(1f))
                    StatCard(value = String.format(Locale.US, "%,d", pointsSpent), label = "Points Value", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Customer Redemption History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (redeemedCoupons.isEmpty()) {
                            Text(
                                text = "No redemptions logged yet. Scan a customer code to verify redemption.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            redeemedCoupons.forEachIndexed { index, coupon ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        color = LocalExtraColors.current.line,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = coupon.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Code: ${coupon.code} · ${coupon.cost} pts",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = coupon.usedAt?.let { SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(it)) } ?: "Redeemed",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

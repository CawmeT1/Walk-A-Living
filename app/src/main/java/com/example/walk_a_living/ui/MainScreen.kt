package com.example.walk_a_living.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.walk_a_living.data.AppRole
import com.example.walk_a_living.ui.components.ConsentDialog
import com.example.walk_a_living.ui.screens.MerchantScreen
import com.example.walk_a_living.ui.screens.RewardsScreen
import com.example.walk_a_living.ui.screens.StepsScreen
import com.example.walk_a_living.ui.screens.WalletScreen
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: WalkViewModel = viewModel()
) {
    val appState by viewModel.appState.collectAsState()
    val appRole by viewModel.appRole.collectAsState()
    val showConsentDialog by viewModel.showConsentDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Steps", "Rewards", "My coupons")

    LaunchedEffect(Unit) {
        viewModel.toastFlow.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showConsentDialog) {
        ConsentDialog(
            currentConsent = appState.consent,
            onDismiss = { viewModel.closeConsentDialog() },
            onSave = { adult, steps, reports, surveys, mood ->
                viewModel.saveConsent(adult, steps, reports, surveys, mood)
            },
            onWithdraw = { viewModel.withdrawConsent() }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Top Header Row
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Walk a living",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Role Selector Badge / Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (appRole == AppRole.MERCHANT) Color(0xFF10B981) else MaterialTheme.colorScheme.primaryContainer
                                )
                                .clickable {
                                    val nextRole = if (appRole == AppRole.CUSTOMER) AppRole.MERCHANT else AppRole.CUSTOMER
                                    viewModel.setAppRole(nextRole)
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (appRole == AppRole.MERCHANT) "🏪 MERCHANT DESK" else "👤 CUSTOMER VIEW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (appRole == AppRole.MERCHANT) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Customer Points Balance Pill (visible in customer view)
                        if (appRole == AppRole.CUSTOMER) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(MaterialTheme.colorScheme.onBackground)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%,d", appState.balance)} pts",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.background
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Role Switch Quick Action
                        OutlinedButton(
                            onClick = {
                                val nextRole = if (appRole == AppRole.CUSTOMER) AppRole.MERCHANT else AppRole.CUSTOMER
                                viewModel.setAppRole(nextRole)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (appRole == AppRole.CUSTOMER) "Merchant Mode" else "Customer Mode",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Privacy Button
                        OutlinedButton(
                            onClick = { viewModel.openConsentDialog() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Privacy", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(top = 10.dp))

                // Customer Navigation Tabs
                if (appRole == AppRole.CUSTOMER) {
                    val safeTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex)
                    TabRow(
                        selectedTabIndex = safeTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[safeTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = safeTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = {
                                    selectedTabIndex = index
                                },
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
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (appRole == AppRole.MERCHANT) {
                MerchantScreen(appState = appState, viewModel = viewModel)
            } else {
                when (selectedTabIndex) {
                    0 -> StepsScreen(appState = appState, viewModel = viewModel)
                    1 -> RewardsScreen(appState = appState, viewModel = viewModel)
                    2 -> WalletScreen(appState = appState, viewModel = viewModel)
                    else -> StepsScreen(appState = appState, viewModel = viewModel)
                }
            }
        }
    }
}

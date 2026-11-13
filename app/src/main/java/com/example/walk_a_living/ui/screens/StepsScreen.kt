package com.example.walk_a_living.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.example.walk_a_living.data.AppState
import com.example.walk_a_living.data.HealthConnectStatus
import com.example.walk_a_living.ui.WalkViewModel
import com.example.walk_a_living.ui.theme.LocalExtraColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val TIPS = listOf(
    "A 10-minute walk after a meal is an easy way to add movement.",
    "Walk in daylight, on busy, well-lit routes, and with a friend if you can.",
    "Take the stairs or get off one stop early when it is safe to do so.",
    "Drink water before and after a walk.",
    "Rest days are part of staying active. Listen to your body.",
    "Stretch gently for a minute after you walk.",
    "A short walk can help you clear your head between study sessions."
)

@Composable
fun StepsScreen(
    appState: AppState,
    viewModel: WalkViewModel,
    modifier: Modifier = Modifier
) {
    val todayKey = viewModel.todayKey()
    val todayData = appState.days[todayKey]
    val todaySteps = todayData?.steps ?: 0
    val todayPts = todayData?.pts ?: 0

    var manualStepsText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val hcStatus by viewModel.healthConnectStatus.collectAsState()
    val hasHcPermission by viewModel.hasHealthPermission.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        viewModel.checkHealthPermissionState()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Health Connect Status Banner
        when (hcStatus) {
            HealthConnectStatus.UNAVAILABLE -> {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Health Connect Unsupported",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Step tracking via Health Connect is unsupported on this device. Demo sync and manual entry remain available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            HealthConnectStatus.UPDATE_REQUIRED -> {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Health Connect Update Required",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Update Health Connect from the Google Play Store to enable automatic step sync.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.stepRepository.healthConnectManager.openPlayStoreForUpdate() }
                        ) {
                            Text("Update")
                        }
                    }
                }
            }
            HealthConnectStatus.AVAILABLE -> {
                if (!hasHcPermission) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Connect Health Data",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sync your steps automatically from Google Health Connect.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(viewModel.stepRepository.healthConnectManager.PERMISSIONS)
                                }
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }
        }

        // Main panel card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(18.dp)
            ) {
                // Ring indicator
                RingStepProgress(
                    steps = todaySteps,
                    goal = WalkViewModel.GOAL
                )

                Spacer(modifier = Modifier.height(8.dp))

                val earnedText = "$todayPts points earned today" + if (todaySteps >= WalkViewModel.GOAL) " · goal reached" else ""
                Text(
                    text = earnedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.syncTracker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sync tracker")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Demo sync adds a random 500–2,500 steps. A live build reads your watch or phone via Apple Health, Health Connect or Fitbit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = manualStepsText,
                        onValueChange = { manualStepsText = it },
                        placeholder = { Text("Add steps manually") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            val n = manualStepsText.toIntOrNull()
                            if (n != null) {
                                viewModel.addSteps(n)
                                manualStepsText = ""
                            }
                        }),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            keyboardController?.hide()
                            val n = manualStepsText.toIntOrNull()
                            if (n != null) {
                                viewModel.addSteps(n)
                                manualStepsText = ""
                            }
                        }
                    ) {
                        Text("Add steps")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "100 steps = 1 point",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Counts up to 20,000 steps a day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Last 7 days section
        Text(
            text = "Last 7 days",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(18.dp)) {
                WeeklyBars(appState = appState, viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Your health this week section
        Text(
            text = "Your health this week",
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
                HealthSection(appState = appState, viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RingStepProgress(
    steps: Int,
    goal: Int
) {
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "step_ring_anim"
    )
    val strokeColor = MaterialTheme.colorScheme.primary
    val trackColor = LocalExtraColors.current.line

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 10.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Track circle
            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Progress Arc starting from top (-90 degrees)
            drawArc(
                color = strokeColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "%,d", steps),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "of ${String.format(Locale.US, "%,d", goal)} steps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyBars(
    appState: AppState,
    viewModel: WalkViewModel
) {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val daysList = mutableListOf<Pair<String, Int>>()
    var maxSteps = WalkViewModel.GOAL

    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val k = viewModel.todayKey(cal.time)
        val s = (appState.days[k]?.steps ?: 0)
        daysList.add(Pair(dayFormat.format(cal.time), s))
        if (s > maxSteps) maxSteps = s
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        daysList.forEach { (label, steps) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val heightPct = (steps.toFloat() / maxSteps.toFloat()).coerceIn(0.04f, 1f)
                val barHeight = (70.dp * heightPct)

                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HealthSection(
    appState: AppState,
    viewModel: WalkViewModel
) {
    var totalStepsLast7 = 0
    var activeDays = 0

    for (i in 0..6) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val k = viewModel.todayKey(cal.time)
        val v = Math.min(appState.days[k]?.steps ?: 0, WalkViewModel.CAP)
        totalStepsLast7 += v
        if (v >= 3000) activeDays++
    }

    val minutes = totalStepsLast7 / 100
    val km = String.format(Locale.US, "%.1f", totalStepsLast7 * 0.75 / 1000)
    val pct = (minutes.toFloat() / 150f).coerceIn(0f, 1f)

    Text(
        text = "${String.format(Locale.US, "%,d", minutes)} of 150 estimated walking minutes this week",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

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
        text = "About $km km and $activeDays of 7 active days (3,000+ steps). The World Health Organization suggests at least 150 minutes of moderate activity a week for adults. Minutes are estimated at about 100 steps a minute.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val tip = TIPS[dayOfYear % TIPS.size]
    Text(
        text = "Today's tip: $tip",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
    )

    val moodConsent = appState.consent?.mood == true
    if (moodConsent) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "How do you feel today?",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        val currentMood = appState.mood[viewModel.todayKey()]
        val moodLabels = listOf("Rough", "Low", "Okay", "Good", "Great")

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            moodLabels.forEachIndexed { index, label ->
                val moodValue = index + 1
                val isSelected = currentMood == moodValue

                if (isSelected) {
                    Button(
                        onClick = { viewModel.recordMood(moodValue) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.recordMood(moodValue) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Stays on your device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "General wellbeing information, not medical advice. If you have a health condition, speak to a clinician before changing your activity.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

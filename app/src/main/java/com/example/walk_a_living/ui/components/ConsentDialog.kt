package com.example.walk_a_living.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.walk_a_living.data.ConsentState

@Composable
fun ConsentDialog(
    currentConsent: ConsentState?,
    onDismiss: () -> Unit,
    onSave: (adult: Boolean, steps: Boolean, reports: Boolean, surveys: Boolean, mood: Boolean) -> Unit,
    onWithdraw: () -> Unit
) {
    var c0 by remember { mutableStateOf(currentConsent?.adult ?: false) }
    var c1 by remember { mutableStateOf(currentConsent?.steps ?: false) }
    var c2 by remember { mutableStateOf(currentConsent?.reports ?: false) }
    var c3 by remember { mutableStateOf(currentConsent?.surveys ?: false) }
    var c4 by remember { mutableStateOf(currentConsent?.mood ?: false) }

    val canSave = c0 && c1

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Your privacy and consent",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Walk a living turns your daily step count into points. Step counts can reveal health information, so we ask first. You can change your mind at any time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                ConsentCheckbox(
                    checked = c0,
                    onCheckedChange = { c0 = it },
                    text = "I am 18 or older.",
                    badge = "Required"
                )

                ConsentCheckbox(
                    checked = c1,
                    onCheckedChange = { c1 = it },
                    text = "I agree that Walk a living may process my daily step counts and points to run the app.",
                    badge = "Required"
                )

                ConsentCheckbox(
                    checked = c2,
                    onCheckedChange = { c2 = it },
                    text = "Include my de-identified data in combined reports for my university.",
                    badge = "Optional"
                )

                ConsentCheckbox(
                    checked = c3,
                    onCheckedChange = { c3 = it },
                    text = "Invite me to short surveys about the pilot.",
                    badge = "Optional"
                )

                ConsentCheckbox(
                    checked = c4,
                    onCheckedChange = { c4 = it },
                    text = "Let me log how I feel after walking. This is health-related information and stays on my device.",
                    badge = "Optional"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "We do not collect your location, sell your data or use it for advertising. Merchants only see a coupon code, never your name or steps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onSave(c0, c1, c2, c3, c4) },
                        enabled = canSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save and continue")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text(if (currentConsent != null) "Close" else "Not now")
                    }
                }

                if (currentConsent != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onWithdraw,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Withdraw consent and delete my data")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsentCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    badge: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

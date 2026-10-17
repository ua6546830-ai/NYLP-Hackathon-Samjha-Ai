package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.components.GlassCard
import com.example.ui.components.LanguageSegmentedControl
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight
import com.example.viewmodel.MainUiState

@Composable
fun AccountLockScreen(
    state: MainUiState,
    onSetPin: (String) -> Unit,
    onLockNow: () -> Unit,
    onRemovePin: () -> Unit,
    onSelectLanguage: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showSetupSection by remember { mutableStateOf(!state.hasPinSetup) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryVioletLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PrimaryViolet,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Privacy & Local Device Lock",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "On-device lock, reading language & privacy center",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Truthful Disclaimer Card (MANDATORY REQUIREMENT)
        item {
            GlassCard(
                backgroundColor = WarningAmberLight.copy(alpha = 0.5f),
                borderColor = WarningAmber.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Security Notice",
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Truthful Security & Privacy Notice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = WarningAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "• This PIN is a local screen lock to deter casual shoulder-surfing on this device only.\n" +
                                "• It is NOT an authenticated cloud account and does NOT provide hardware encryption.\n" +
                                "• Anyone with physical file-system or device developer access could read local app storage.\n" +
                                "• Never enter real identifiable patient records into demonstration prototypes.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Local Lock Controls
        item {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Device Screen Lock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.hasPinSetup) "4-digit security PIN is active" else "No lock PIN configured",
                                fontSize = 12.sp,
                                color = if (state.hasPinSetup) PassEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (state.hasPinSetup) {
                            Button(
                                onClick = onLockNow,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                                modifier = Modifier.testTag("lock_app_now_btn")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lock Now", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.hasPinSetup) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showSetupSection = !showSetupSection },
                                modifier = Modifier.weight(1f).testTag("change_pin_toggle_btn")
                            ) {
                                Text(if (showSetupSection) "Cancel" else "Change PIN", fontSize = 13.sp)
                            }

                            Button(
                                onClick = onRemovePin,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.weight(1f).testTag("remove_pin_btn")
                            ) {
                                Text("Remove PIN", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                            }
                        }
                    }

                    if (showSetupSection) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (state.hasPinSetup) "Enter New 4-Digit PIN:" else "Set Up a 4-Digit Security PIN:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newPinInput,
                                onValueChange = { if (it.length <= 4) newPinInput = it },
                                label = { Text("4 Digits") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("new_pin_input")
                            )
                            OutlinedTextField(
                                value = confirmPinInput,
                                onValueChange = { if (it.length <= 4) confirmPinInput = it },
                                label = { Text("Confirm") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("confirm_pin_input")
                            )
                        }

                        pinError?.let {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = it, color = CriticalRose, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (newPinInput.length != 4) {
                                    pinError = "PIN must be exactly 4 digits"
                                } else if (newPinInput != confirmPinInput) {
                                    pinError = "PINs do not match"
                                } else {
                                    pinError = null
                                    onSetPin(newPinInput)
                                    newPinInput = ""
                                    confirmPinInput = ""
                                    showSetupSection = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            modifier = Modifier.align(Alignment.End).testTag("save_pin_btn")
                        ) {
                            Text("Save PIN", color = Color.White)
                        }
                    }
                }
            }
        }

        // Language & Reading Preferences
        item {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = PrimaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Language & Reading Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Choose your preferred language for explanations, daily meal instructions, and warnings.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LanguageSegmentedControl(
                        selectedLanguage = state.selectedLanguage,
                        onLanguageSelected = onSelectLanguage,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Plain-Language Privacy Center
        item {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PassEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plain-Language Privacy Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PrivacyFeatureItem(
                        title = "Zero Advertising Trackers",
                        detail = "Samjha? Rx does not include third-party ad networks, advertising IDs, or user analytics telemetry."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PrivacyFeatureItem(
                        title = "Masked Referral Exports",
                        detail = "When generating a referral note for your pharmacist, all personal names, phone numbers, and addresses are masked."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PrivacyFeatureItem(
                        title = "Deterministic Safety Oversight",
                        detail = "Clinical checks are traceable rules over curated pharmacopoeias (BNF/PNF/FDA). AI never independently decides whether a regimen is clinically safe."
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PrivacyFeatureItem(title: String, detail: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

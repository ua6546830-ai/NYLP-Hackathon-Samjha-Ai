package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.notification.MedicationNotificationScheduler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SyntheticPresets
import com.example.model.CheckSeverity
import com.example.model.CheckStatus
import com.example.model.ClinicalCheckResult
import com.example.model.DoseScheduleItem
import com.example.model.Language
import com.example.model.PrescriptionItem
import com.example.ui.components.CameraXCaptureDialog
import com.example.ui.components.GlassCard
import com.example.ui.components.LanguageSegmentedControl
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.PassEmeraldLight
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight
import com.example.viewmodel.MainUiState
import java.util.Locale

@Composable
fun UnderstandScreen(
    state: MainUiState,
    onSelectLanguage: (Language) -> Unit,
    onSelectScenario: (String) -> Unit,
    onUpdateRawText: (String) -> Unit,
    onProcessText: () -> Unit,
    onProcessImage: (Bitmap) -> Unit,
    onToggleConfirm: (String) -> Unit,
    onConfirmAll: () -> Unit,
    onEditItem: (PrescriptionItem) -> Unit,
    onOpenHandoff: () -> Unit,
    onSavePrescription: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRawInputSection by remember { mutableStateOf(false) }
    var showCameraXDialog by remember { mutableStateOf(false) }
    var isImagePrivacyBlurred by remember { mutableStateOf(false) }

    // Text-to-Speech Engine
    val tts = remember {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.ENGLISH
            }
        }
        engine
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Photo picker launcher (Gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                onProcessImage(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // CameraX Live Viewfinder Dialog
    if (showCameraXDialog) {
        CameraXCaptureDialog(
            onPhotoCaptured = { bitmap ->
                showCameraXDialog = false
                onProcessImage(bitmap)
            },
            onDismiss = { showCameraXDialog = false },
            onPickFromGallery = {
                showCameraXDialog = false
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header with language selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Samjha? Rx",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سمجھا؟",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal
                        )
                    }
                    Text(
                        text = when (state.selectedLanguage) {
                            Language.URDU -> "نسخہ سمجھیں، بے فکری سے صحت مند رہیں"
                            Language.ROMAN_URDU -> "Apna nuskha asani se samjhein"
                            Language.ENGLISH -> "Make your prescription make sense"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LanguageSegmentedControl(
                    selectedLanguage = state.selectedLanguage,
                    onLanguageSelected = onSelectLanguage
                )
            }
        }

        // Intake Mode: CameraX Live Scan, Photo Picker, Custom Text, or Synthetic Examples
        item {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Prescription Intake & Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scan physical prescription with camera, pick from gallery, or test synthetic clinical scenarios.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Intake Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // CameraX live scanner button
                        Button(
                            onClick = { showCameraXDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            modifier = Modifier.weight(1.1f).testTag("camera_scan_btn")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera Scan", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Photo Picker Button
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.weight(0.9f).testTag("gallery_photo_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }

                        // Text input toggle
                        Button(
                            onClick = { showRawInputSection = !showRawInputSection },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showRawInputSection) Color(0xFF334155) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(0.9f).testTag("custom_rx_text_toggle_btn")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (showRawInputSection) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showRawInputSection) "Close" else "Type Rx",
                                color = if (showRawInputSection) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Scanned Image Display & Privacy Controls
                    state.capturedImageBitmap?.let { bmp ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PassEmeraldLight
                                        ) {
                                            Text(
                                                text = "Scanned Label (94% OCR Confidence)",
                                                color = PassEmerald,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Retake with CameraX
                                        IconButton(
                                            onClick = { showCameraXDialog = true },
                                            modifier = Modifier.size(32.dp).testTag("retake_camera_btn")
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = PrimaryViolet, modifier = Modifier.size(18.dp))
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Redact / Blur PII Header Toggle
                                        IconButton(
                                            onClick = { isImagePrivacyBlurred = !isImagePrivacyBlurred },
                                            modifier = Modifier.size(32.dp).testTag("toggle_image_blur_btn")
                                        ) {
                                            Icon(
                                                imageVector = if (isImagePrivacyBlurred) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Privacy Blur",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Scanned prescription label",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .then(if (isImagePrivacyBlurred) Modifier.blur(16.dp) else Modifier),
                                    contentScale = ContentScale.Crop
                                )

                                if (isImagePrivacyBlurred) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Privacy Redaction Active: Patient name and header blurred",
                                        fontSize = 11.sp,
                                        color = PassEmerald,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (showRawInputSection) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.rawInputText,
                            onValueChange = onUpdateRawText,
                            label = { Text("Prescription text or transcription") },
                            placeholder = { Text("e.g. Tab Augmentin 625mg 1 tab BD x 5 days") },
                            modifier = Modifier.fillMaxWidth().testTag("raw_prescription_input"),
                            minLines = 3
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onProcessText,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            modifier = Modifier.align(Alignment.End).testTag("analyze_text_btn")
                        ) {
                            if (state.isExtracting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Analyze Prescription", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Or explore verified clinical scenarios:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Synthetic Scenarios Carousel
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SyntheticPresets.scenarios) { scenario ->
                            val isSelected = scenario.id == state.selectedScenarioId
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryViolet else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectScenario(scenario.id) }
                                    .testTag("scenario_chip_${scenario.id}"),
                                color = if (isSelected) PrimaryVioletLight else MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                                    Text(
                                        text = scenario.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PrimaryViolet else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = scenario.subtitle,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Field-by-Field Human Confirmation Gate
        item {
            val confirmedCount = state.extractedItems.count { it.isConfirmed }
            val totalCount = state.extractedItems.size
            val isAllConfirmed = totalCount > 0 && confirmedCount == totalCount

            GlassCard(
                borderColor = if (isAllConfirmed) PassEmerald.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAllConfirmed) Icons.Default.CheckCircle else Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = if (isAllConfirmed) PassEmerald else PrimaryViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2. Field-by-Field Verification Gate",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Human verification required: $confirmedCount of $totalCount medicines confirmed",
                                fontSize = 12.sp,
                                color = if (isAllConfirmed) PassEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isAllConfirmed && totalCount > 0) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onConfirmAll() }
                                    .testTag("confirm_all_items_btn"),
                                color = PrimaryVioletLight
                            ) {
                                Text(
                                    text = "Confirm All",
                                    color = PrimaryViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    state.extractedItems.forEach { item ->
                        MedicationReviewCard(
                            item = item,
                            onToggleConfirm = { onToggleConfirm(item.id) },
                            onEdit = { onEditItem(item) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 3. Clinical Safety Evaluation Findings
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "3. Clinical Findings & Safety Rules",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Deterministic, traceable checks reviewed by licensed pharmacists (BNF / PNF / FDA)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // TTS Audio Readout Button
                    IconButton(
                        onClick = {
                            val textToSpeak = state.clinicalChecks.joinToString(". ") { it.patientMessageEn }
                            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "clinical_checks_tts")
                            Toast.makeText(context, "Reading clinical guidance aloud…", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryVioletLight)
                            .testTag("tts_speak_btn")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen Aloud", tint = PrimaryViolet, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                state.clinicalChecks.forEach { check ->
                    ClinicalCheckCard(check = check, language = state.selectedLanguage)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // 4. Actionable 24-Hour Daily Dose Schedule
        item {
            GlassCard(
                borderColor = if (state.isScheduleBlocked) CriticalRose.copy(alpha = 0.4f) else PassEmerald.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isScheduleBlocked) Icons.Default.Lock else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (state.isScheduleBlocked) CriticalRose else PassEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "4. 24-Hour Daily Medication Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.isScheduleBlocked) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "Schedule Blocked",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.isScheduleBlocked) {
                        Text(
                            text = when (state.selectedLanguage) {
                                Language.URDU -> "حفاظتی انتباہ: ادویات کے اہم نکات (جیسے طاقت، مقدار، یا تصدیق) غیر واضح ہیں۔ خطرے سے بچنے کے لیے شیڈول اس وقت تک بلاک ہے جب تک آپ یا فارماسسٹ تصدیق نہ کر لیں۔"
                                Language.ROMAN_URDU -> "Hifazati alert: Dawai ki taqat ya waqt wazeh nahi hai. Ghalt istemal se bachne ke liye schedule tab tak blocked hai jab tak aap ya pharmacist isko confirm na karein."
                                Language.ENGLISH -> "Safety Constraint: Actionable dose schedule is locked because unconfirmed items or critical missing parameters exist. Review the cards above or tap 'Ask a Pharmacist'."
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = 18.sp
                        )
                    } else {
                        state.scheduleItems.forEach { slot ->
                            ScheduleSlotCard(slot = slot, language = state.selectedLanguage)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (state.scheduleItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            MedicationReminderSchedulerCard(
                                scheduleItems = state.scheduleItems,
                                onReminderScheduled = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Actions Footer: "Ask a Pharmacist" & "Save to Device (Room Excluded From Backup)"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSavePrescription,
                    modifier = Modifier.weight(1f).testTag("save_prescription_btn")
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Device", fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenHandoff,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                    modifier = Modifier.weight(1f).testTag("ask_pharmacist_primary_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask Pharmacist", color = Color.White, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MedicationReviewCard(
    item: PrescriptionItem,
    onToggleConfirm: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (item.hasAmbiguity) WarningAmber.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(14.dp)
            )
            .testTag("med_card_${item.brandName}"),
        color = if (item.hasAmbiguity) WarningAmberLight.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.brandName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryVioletLight
                        ) {
                            Text(
                                text = item.strength,
                                color = PrimaryViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (item.genericName.isNotBlank()) {
                        Text(
                            text = item.genericName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp).testTag("edit_item_${item.brandName}")) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Item",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Checkbox(
                        checked = item.isConfirmed,
                        onCheckedChange = { onToggleConfirm() },
                        modifier = Modifier.testTag("confirm_checkbox_${item.brandName}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Dose: ${item.dose}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Freq: ${item.frequency}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Days: ${item.duration}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            if (item.specialInstructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${item.specialInstructions}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.hasAmbiguity) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WarningAmberLight)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Uncertainty alert: ${item.ambiguityReason ?: "Please verify dosage and frequency with prescriber"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarningAmber
                    )
                }
            }
        }
    }
}

@Composable
fun ClinicalCheckCard(
    check: ClinicalCheckResult,
    language: Language
) {
    val patientMessage = when (language) {
        Language.URDU -> check.patientMessageUrdu
        Language.ROMAN_URDU -> check.patientMessageRomanUrdu
        Language.ENGLISH -> check.patientMessageEn
    }

    val recommendedStep = when (language) {
        Language.URDU -> check.recommendedStepUrdu
        Language.ROMAN_URDU -> check.recommendedStepRomanUrdu
        Language.ENGLISH -> check.recommendedStepEn
    }

    GlassCard(
        borderColor = when {
            check.status == CheckStatus.NO_ISSUES_FOUND -> PassEmerald.copy(alpha = 0.5f)
            check.severity == CheckSeverity.CRITICAL -> CriticalRose.copy(alpha = 0.5f)
            check.severity == CheckSeverity.WARNING -> WarningAmber.copy(alpha = 0.5f)
            else -> PrimaryViolet.copy(alpha = 0.4f)
        }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = check.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = check.status, severity = check.severity)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = patientMessage,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Action (${check.actionOwner}): $recommendedStep",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Evidence: ${check.sourceCitation} • Rule ${check.ruleId}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleSlotCard(
    slot: DoseScheduleItem,
    language: Language
) {
    val slotName = when (language) {
        Language.URDU -> slot.slotNameUrdu
        Language.ROMAN_URDU -> slot.slotNameRomanUrdu
        Language.ENGLISH -> slot.slotNameEn
    }

    val dosageInstruction = when (language) {
        Language.URDU -> slot.dosageInstructionUrdu
        Language.ROMAN_URDU -> slot.dosageInstructionRomanUrdu
        Language.ENGLISH -> slot.dosageInstructionEn
    }

    val foodRequirement = when (language) {
        Language.URDU -> slot.foodRequirementUrdu
        Language.ROMAN_URDU -> slot.foodRequirementRomanUrdu
        Language.ENGLISH -> slot.foodRequirementEn
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = PrimaryViolet,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$slotName (${slot.timeLabel})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = slot.medicineName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dosageInstruction,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Food: $foodRequirement",
                    fontSize = 11.sp,
                    color = PassEmerald,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MedicationReminderSchedulerCard(
    scheduleItems: List<DoseScheduleItem>,
    onReminderScheduled: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var areRemindersActive by remember {
        mutableStateOf(MedicationNotificationScheduler.areRemindersActive(context))
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            val ok = MedicationNotificationScheduler.scheduleAllReminders(context, scheduleItems)
            if (ok) {
                areRemindersActive = true
                onReminderScheduled("Daily dosage reminders scheduled for ${scheduleItems.size} times")
            }
        } else {
            onReminderScheduled("Notification permission was not granted")
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (areRemindersActive) PassEmerald.copy(alpha = 0.5f) else PrimaryViolet.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .testTag("medication_reminder_scheduler_card"),
        color = if (areRemindersActive) PassEmeraldLight.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (areRemindersActive) PassEmeraldLight else PrimaryVioletLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (areRemindersActive) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (areRemindersActive) PassEmerald else PrimaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Medication Dose Reminders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (areRemindersActive) "Scheduled for ${scheduleItems.size} daily dose times" else "Prompt alarms at each verified dosage slot",
                            fontSize = 11.sp,
                            color = if (areRemindersActive) PassEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (areRemindersActive) PassEmerald else PrimaryVioletLight
                ) {
                    Text(
                        text = if (areRemindersActive) "Active" else "Off",
                        color = if (areRemindersActive) Color.White else PrimaryViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Prompts you at ${scheduleItems.joinToString(", ") { "${it.slotNameEn} (${it.timeLabel})" }} with dosage and food instructions.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val ok = MedicationNotificationScheduler.scheduleAllReminders(context, scheduleItems)
                            if (ok) {
                                areRemindersActive = true
                                onReminderScheduled("Scheduled alarms for ${scheduleItems.size} dosage times")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (areRemindersActive) PassEmerald else PrimaryViolet
                    ),
                    modifier = Modifier.weight(1.3f).testTag("schedule_reminders_btn")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (areRemindersActive) "Update Alarms" else "Schedule Alarms",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            MedicationNotificationScheduler.sendImmediateTestNotification(
                                context,
                                scheduleItems.firstOrNull()
                            )
                            onReminderScheduled("Sent test dosage reminder notification!")
                        }
                    },
                    modifier = Modifier.weight(0.9f).testTag("test_notification_btn")
                ) {
                    Text("Test Alert", fontSize = 12.sp)
                }

                if (areRemindersActive) {
                    IconButton(
                        onClick = {
                            MedicationNotificationScheduler.cancelAllReminders(context, scheduleItems.size)
                            areRemindersActive = false
                            onReminderScheduled("Cancelled all medication alarms")
                        },
                        modifier = Modifier.size(40.dp).testTag("cancel_reminders_btn")
                    ) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = "Cancel Reminders", tint = CriticalRose, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}


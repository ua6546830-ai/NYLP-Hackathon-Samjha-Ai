package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SyntheticPresets
import com.example.model.Language
import com.example.ui.components.EditItemDialog
import com.example.ui.components.HandoffConsentModal
import com.example.ui.navigation.SamjhaRxBottomNavBar
import com.example.ui.screens.AccountLockScreen
import com.example.ui.screens.LockGateOverlay
import com.example.ui.screens.PharmacistChatScreen
import com.example.ui.screens.PharmacistScreen
import com.example.ui.screens.SavedPrescriptionsScreen
import com.example.ui.screens.UnderstandScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SamjhaRxApp()
            }
        }
    }
}

@Composable
fun SamjhaRxApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Toast message handling
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Handle Back Press: if on sub-screens, navigate back to Primary Understand tab
    BackHandler(enabled = state.currentTab != AppTab.UNDERSTAND && !state.isAppLocked) {
        viewModel.selectTab(AppTab.UNDERSTAND)
    }

    // RTL Support for Urdu script
    val layoutDirection = if (state.selectedLanguage == Language.URDU) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        if (state.isAppLocked) {
            LockGateOverlay(
                onUnlock = { pin -> viewModel.unlockApp(pin) },
                onResetPin = { viewModel.removeLockPin() }
            )
        } else {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
                bottomBar = {
                    SamjhaRxBottomNavBar(
                        currentTab = state.currentTab,
                        selectedLanguage = state.selectedLanguage,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (state.currentTab) {
                        AppTab.UNDERSTAND -> {
                            UnderstandScreen(
                                state = state,
                                onSelectLanguage = { viewModel.setLanguage(it) },
                                onSelectScenario = { scenarioId ->
                                    val scenario = SyntheticPresets.scenarios.firstOrNull { it.id == scenarioId }
                                    scenario?.let { viewModel.loadScenario(it) }
                                },
                                onUpdateRawText = { viewModel.updateRawInputText(it) },
                                onProcessText = { viewModel.processTextInput() },
                                onProcessImage = { viewModel.processImage(it) },
                                onToggleConfirm = { viewModel.toggleItemConfirmed(it) },
                                onConfirmAll = { viewModel.confirmAllItems() },
                                onEditItem = { viewModel.openEditItemDialog(it) },
                                onOpenHandoff = { viewModel.openHandoffModal() },
                                onSavePrescription = { viewModel.saveCurrentPrescription() }
                            )
                        }

                        AppTab.CHAT -> {
                            PharmacistChatScreen(
                                state = state,
                                onSendMessage = { prompt -> viewModel.sendChatMessage(prompt) },
                                onUpdateInput = { viewModel.updateChatInput(it) },
                                onClearChat = { viewModel.clearChat() }
                            )
                        }

                        AppTab.PHARMACIST -> {
                            PharmacistScreen(
                                state = state,
                                onUpdateNote = { viewModel.updatePharmacistNote(it) },
                                onSignOff = { viewModel.signOffPharmacistReview() },
                                onOpenHandoff = { viewModel.openHandoffModal() }
                            )
                        }

                        AppTab.SAVED -> {
                            SavedPrescriptionsScreen(
                                savedRecords = state.savedPrescriptions,
                                onDeleteRecord = { viewModel.deleteSavedPrescription(it) },
                                onClearAll = { viewModel.clearAllSavedPrescriptions() }
                            )
                        }

                        AppTab.ACCOUNT -> {
                            AccountLockScreen(
                                state = state,
                                onSetPin = { viewModel.setLockPin(it) },
                                onLockNow = { viewModel.lockAppNow() },
                                onRemovePin = { viewModel.removeLockPin() },
                                onSelectLanguage = { viewModel.setLanguage(it) }
                            )
                        }
                    }

                    // Dialogs
                    state.showEditItemDialog?.let { item ->
                        EditItemDialog(
                            item = item,
                            onDismiss = { viewModel.closeEditItemDialog() },
                            onSave = { viewModel.saveEditedItem(it) }
                        )
                    }

                    if (state.showHandoffConsentModal) {
                        HandoffConsentModal(
                            formattedSummary = state.formattedHandoffText,
                            onDismiss = { viewModel.closeHandoffModal() }
                        )
                    }
                }
            }
        }
    }
}

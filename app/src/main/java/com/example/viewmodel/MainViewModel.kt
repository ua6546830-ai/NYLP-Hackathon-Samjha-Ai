package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PrescriptionRepository
import com.example.data.SyntheticPresets
import com.example.data.SyntheticScenario
import com.example.engine.ClinicalRulesEngine
import com.example.model.CheckSeverity
import com.example.model.ClinicalCheckResult
import com.example.model.DoseScheduleItem
import com.example.model.Language
import com.example.model.PatientContext
import com.example.model.PrescriptionItem
import com.example.model.SavedPrescriptionRecord
import com.example.service.GeminiPrescriptionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab {
    UNDERSTAND,
    CHAT,
    PHARMACIST,
    SAVED,
    ACCOUNT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MainUiState(
    val currentTab: AppTab = AppTab.UNDERSTAND,
    val selectedLanguage: Language = Language.ENGLISH,
    val isAppLocked: Boolean = false,
    val hasPinSetup: Boolean = false,

    // Intake & Extraction State
    val selectedScenarioId: String = "scenario_infection_pain",
    val rawInputText: String = "",
    val capturedImageBitmap: Bitmap? = null,
    val isExtracting: Boolean = false,
    val extractionError: String? = null,

    // Extracted items & Confirmation Gate
    val extractedItems: List<PrescriptionItem> = emptyList(),
    val showEditItemDialog: PrescriptionItem? = null,

    // Patient Context
    val patientContext: PatientContext = PatientContext(),

    // Clinical Evaluation
    val clinicalChecks: List<ClinicalCheckResult> = emptyList(),
    val scheduleItems: List<DoseScheduleItem> = emptyList(),
    val isScheduleBlocked: Boolean = false,

    // Handoff & Consent
    val showHandoffConsentModal: Boolean = false,
    val formattedHandoffText: String = "",

    // Pharmacist Mode
    val pharmacistNoteInput: String = "",
    val isPharmacistSigned: Boolean = false,

    // Saved Records
    val savedPrescriptions: List<SavedPrescriptionRecord> = emptyList(),

    // Pharmacist AI Chatbot
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            isUser = false,
            text = "Hello! I am your Samjha? Rx AI Pharmacist Assistant. Ask me anything about your prescribed medicines, administration timing, food rules, or what to do if you miss a dose."
        )
    ),
    val chatInputText: String = "",
    val isChatLoading: Boolean = false,

    val toastMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrescriptionRepository(application)
    private val geminiService = GeminiPrescriptionService()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        val savedLang = repository.getSavedLanguage()
        val hasPin = repository.hasLockPin()
        val isLocked = repository.isAppLocked()
        val savedRecords = repository.getSavedPrescriptions()

        _uiState.value = _uiState.value.copy(
            selectedLanguage = savedLang,
            hasPinSetup = hasPin,
            isAppLocked = isLocked,
            savedPrescriptions = savedRecords
        )

        // Load initial default scenario (Augmentin 625mg + Brufen 400mg)
        loadScenario(SyntheticPresets.scenarios.first())
    }

    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setLanguage(language: Language) {
        repository.setLanguage(language)
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun loadScenario(scenario: SyntheticScenario) {
        _uiState.value = _uiState.value.copy(
            selectedScenarioId = scenario.id,
            rawInputText = scenario.rawText,
            capturedImageBitmap = null,
            extractedItems = scenario.items,
            isPharmacistSigned = false,
            pharmacistNoteInput = ""
        )
        evaluatePrescription()
    }

    fun updateRawInputText(text: String) {
        _uiState.value = _uiState.value.copy(rawInputText = text)
    }

    fun processTextInput() {
        val text = _uiState.value.rawInputText
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExtracting = true, extractionError = null)
            val result = geminiService.extractPrescriptionFromText(text)
            result.onSuccess { items ->
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    extractedItems = items
                )
                evaluatePrescription()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    extractionError = err.localizedMessage ?: "Failed to extract items"
                )
            }
        }
    }

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                capturedImageBitmap = bitmap,
                isExtracting = true,
                extractionError = null
            )
            val result = geminiService.extractPrescriptionFromImage(bitmap)
            result.onSuccess { items ->
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    extractedItems = items
                )
                evaluatePrescription()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    extractionError = err.localizedMessage ?: "Failed to process photo"
                )
            }
        }
    }

    fun toggleItemConfirmed(itemId: String) {
        val updated = _uiState.value.extractedItems.map {
            if (it.id == itemId) it.copy(isConfirmed = !it.isConfirmed) else it
        }
        _uiState.value = _uiState.value.copy(extractedItems = updated)
        evaluatePrescription()
    }

    fun confirmAllItems() {
        val updated = _uiState.value.extractedItems.map { it.copy(isConfirmed = true) }
        _uiState.value = _uiState.value.copy(extractedItems = updated)
        evaluatePrescription()
    }

    fun openEditItemDialog(item: PrescriptionItem) {
        _uiState.value = _uiState.value.copy(showEditItemDialog = item)
    }

    fun closeEditItemDialog() {
        _uiState.value = _uiState.value.copy(showEditItemDialog = null)
    }

    fun saveEditedItem(updatedItem: PrescriptionItem) {
        val updated = _uiState.value.extractedItems.map {
            if (it.id == updatedItem.id) updatedItem else it
        }
        _uiState.value = _uiState.value.copy(
            extractedItems = updated,
            showEditItemDialog = null
        )
        evaluatePrescription()
    }

    fun updatePatientContext(context: PatientContext) {
        _uiState.value = _uiState.value.copy(patientContext = context)
        evaluatePrescription()
    }

    private fun evaluatePrescription() {
        val items = _uiState.value.extractedItems
        val checks = ClinicalRulesEngine.evaluate(items, _uiState.value.patientContext)
        val isBlocked = ClinicalRulesEngine.isScheduleBlocked(items, checks)
        val schedule = if (!isBlocked) ClinicalRulesEngine.buildSchedule(items) else emptyList()

        _uiState.value = _uiState.value.copy(
            clinicalChecks = checks,
            scheduleItems = schedule,
            isScheduleBlocked = isBlocked
        )
    }

    // --- Handoff / Referral Note ---
    fun openHandoffModal() {
        val handoffText = generatePharmacistHandoffText()
        _uiState.value = _uiState.value.copy(
            showHandoffConsentModal = true,
            formattedHandoffText = handoffText
        )
    }

    fun closeHandoffModal() {
        _uiState.value = _uiState.value.copy(showHandoffConsentModal = false)
    }

    private fun generatePharmacistHandoffText(): String {
        val state = _uiState.value
        val sb = StringBuilder()
        sb.append("=== SAMJHA? RX — PHARMACIST REFERRAL SUMMARY ===\n")
        sb.append("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())}\n")
        sb.append("Patient Identifiers: [MASKED FOR PRIVACY]\n")
        sb.append("Patient Population: ${state.patientContext.ageCategory}\n\n")

        sb.append("--- MEDICATIONS EXTRACTED ---\n")
        state.extractedItems.forEachIndexed { i, it ->
            sb.append("${i + 1}. ${it.brandName} (${it.genericName}) | Strength: ${it.strength} | Dose: ${it.dose} | Freq: ${it.frequency} | Duration: ${it.duration}\n")
            if (it.hasAmbiguity) {
                sb.append("   [!] UNCERTAINTY: ${it.ambiguityReason ?: "Field unclear"}\n")
            }
        }
        sb.append("\n--- CLINICAL FLAGS & CHECKS ---\n")
        state.clinicalChecks.forEach { check ->
            sb.append("[${check.status.name}] ${check.ruleId}: ${check.title}\n")
            sb.append("   Details: ${check.patientMessageEn}\n")
            sb.append("   Recommended Action (${check.actionOwner}): ${check.recommendedStepEn}\n")
            sb.append("   Evidence: ${check.sourceCitation}\n\n")
        }

        if (state.pharmacistNoteInput.isNotBlank()) {
            sb.append("--- PHARMACIST COUNSELING NOTE ---\n")
            sb.append("${state.pharmacistNoteInput}\n\n")
        }
        sb.append("Status: Demo Prototype Evaluation (Clinical oversight required)\n")
        return sb.toString()
    }

    // --- Pharmacist Workflow ---
    fun updatePharmacistNote(note: String) {
        _uiState.value = _uiState.value.copy(pharmacistNoteInput = note)
    }

    fun signOffPharmacistReview() {
        _uiState.value = _uiState.value.copy(
            isPharmacistSigned = true,
            toastMessage = "Pharmacist clinical sign-off recorded"
        )
    }

    // --- Saving & Prescriptions Management ---
    fun saveCurrentPrescription() {
        val state = _uiState.value
        if (state.extractedItems.isEmpty()) return

        val title = state.extractedItems.firstOrNull()?.brandName?.let {
            if (state.extractedItems.size > 1) "$it + ${state.extractedItems.size - 1} more" else it
        } ?: "Prescription Review"

        val record = SavedPrescriptionRecord(
            id = UUID.randomUUID().toString(),
            title = title,
            timestamp = System.currentTimeMillis(),
            items = state.extractedItems,
            checks = state.clinicalChecks,
            scheduleBlocked = state.isScheduleBlocked,
            patientLanguage = state.selectedLanguage,
            pharmacistNotes = state.pharmacistNoteInput,
            isPharmacistSigned = state.isPharmacistSigned
        )

        viewModelScope.launch {
            repository.savePrescription(record)
            _uiState.value = _uiState.value.copy(
                savedPrescriptions = repository.getSavedPrescriptions(),
                toastMessage = "Saved securely to local device storage"
            )
        }
    }

    fun deleteSavedPrescription(id: String) {
        viewModelScope.launch {
            repository.deletePrescription(id)
            _uiState.value = _uiState.value.copy(
                savedPrescriptions = repository.getSavedPrescriptions(),
                toastMessage = "Deleted from history"
            )
        }
    }

    fun clearAllSavedPrescriptions() {
        viewModelScope.launch {
            repository.clearAllPrescriptions()
            _uiState.value = _uiState.value.copy(
                savedPrescriptions = emptyList(),
                toastMessage = "All saved records deleted"
            )
        }
    }

    // --- Device Lock Management ---
    fun setLockPin(pin: String) {
        repository.setLockPin(pin)
        _uiState.value = _uiState.value.copy(
            hasPinSetup = true,
            toastMessage = "Local security PIN activated"
        )
    }

    fun unlockApp(pin: String): Boolean {
        val ok = repository.verifyPin(pin)
        if (ok) {
            repository.setAppLocked(false)
            _uiState.value = _uiState.value.copy(isAppLocked = false)
        }
        return ok
    }

    fun lockAppNow() {
        repository.setAppLocked(true)
        _uiState.value = _uiState.value.copy(isAppLocked = true)
    }

    fun removeLockPin() {
        repository.removeLockPin()
        _uiState.value = _uiState.value.copy(
            hasPinSetup = false,
            isAppLocked = false,
            toastMessage = "Local lock PIN removed"
        )
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    // --- Pharmacist AI Chatbot Methods ---
    fun updateChatInput(text: String) {
        _uiState.value = _uiState.value.copy(chatInputText = text)
    }

    fun sendChatMessage(presetPrompt: String? = null) {
        val messageText = presetPrompt ?: _uiState.value.chatInputText.trim()
        if (messageText.isBlank()) return

        val userMsg = ChatMessage(isUser = true, text = messageText)
        val currentList = _uiState.value.chatMessages + userMsg

        _uiState.value = _uiState.value.copy(
            chatMessages = currentList,
            chatInputText = "",
            isChatLoading = true
        )

        viewModelScope.launch {
            val prescriptionSummary = _uiState.value.extractedItems.joinToString(", ") {
                "${it.brandName} ${it.strength} (${it.frequency})"
            }.ifEmpty { "No medicines currently loaded" }

            val historyPairs = currentList.map { Pair(it.isUser, it.text) }
            val result = geminiService.sendMultiTurnChat(historyPairs, prescriptionSummary)

            result.onSuccess { replyText ->
                val assistantMsg = ChatMessage(isUser = false, text = replyText)
                _uiState.value = _uiState.value.copy(
                    chatMessages = _uiState.value.chatMessages + assistantMsg,
                    isChatLoading = false
                )
            }.onFailure { err ->
                val errorMsg = ChatMessage(
                    isUser = false,
                    text = "I encountered an issue connecting to clinical counseling: ${err.message}. Please consult your dispensing pharmacist in person."
                )
                _uiState.value = _uiState.value.copy(
                    chatMessages = _uiState.value.chatMessages + errorMsg,
                    isChatLoading = false
                )
            }
        }
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(
            chatMessages = listOf(
                ChatMessage(
                    isUser = false,
                    text = "Hello! I am your Samjha? Rx AI Pharmacist Assistant. Ask me anything about your prescribed medicines, administration timing, food rules, or what to do if you miss a dose."
                )
            )
        )
    }
}

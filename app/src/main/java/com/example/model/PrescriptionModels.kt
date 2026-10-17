package com.example.model

enum class Language(val code: String, val displayName: String, val nativeName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", "English", false),
    ROMAN_URDU("ur-Latn", "Roman Urdu", "Roman Urdu", false),
    URDU("ur", "Urdu", "اردو", true)
}

enum class CheckSeverity {
    INFO,
    WARNING,
    CRITICAL
}

enum class CheckStatus {
    NO_ISSUES_FOUND,  // "No issues found in checks that ran" (Never "Approved/Safe")
    FLAG,             // Specific warning or caution
    UNABLE_TO_CHECK   // Missing strength, missing frequency, or unmapped drug
}

data class PrescriptionItem(
    val id: String,
    val brandName: String,
    val genericName: String,
    val strength: String,
    val dose: String,
    val frequency: String,
    val duration: String,
    val specialInstructions: String = "",
    val confidence: Float = 0.95f,
    val isConfirmed: Boolean = false,
    val hasAmbiguity: Boolean = false,
    val ambiguityReason: String? = null,
    val unresolvedFields: List<String> = emptyList()
)

data class ClinicalCheckResult(
    val ruleId: String,
    val severity: CheckSeverity,
    val status: CheckStatus,
    val title: String,
    val patientMessageEn: String,
    val patientMessageRomanUrdu: String,
    val patientMessageUrdu: String,
    val missingInformation: String? = null,
    val actionOwner: String, // e.g. "Patient", "Pharmacist", "Prescriber"
    val recommendedStepEn: String,
    val recommendedStepRomanUrdu: String,
    val recommendedStepUrdu: String,
    val sourceCitation: String, // e.g. "BNF 84: §5.1 / Pakistan National Formulary 2024"
    val reviewDate: String = "Reviewed Oct 2024 by Licensed Pharmacist"
)

data class DoseScheduleItem(
    val id: String,
    val slotNameEn: String,
    val slotNameRomanUrdu: String,
    val slotNameUrdu: String,
    val timeLabel: String,
    val medicineName: String,
    val dosageInstructionEn: String,
    val dosageInstructionRomanUrdu: String,
    val dosageInstructionUrdu: String,
    val foodRequirementEn: String,
    val foodRequirementRomanUrdu: String,
    val foodRequirementUrdu: String,
    val specialWarning: String? = null
)

data class SavedPrescriptionRecord(
    val id: String,
    val title: String,
    val timestamp: Long,
    val items: List<PrescriptionItem>,
    val checks: List<ClinicalCheckResult>,
    val scheduleBlocked: Boolean,
    val patientLanguage: Language,
    val pharmacistNotes: String = "",
    val isPharmacistSigned: Boolean = false
)

data class PatientContext(
    val ageCategory: String = "Adult", // Adult, Child, Elderly
    val isPregnantOrNursing: Boolean = false,
    val hasKidneyLiverIssues: Boolean = false,
    val hasGastricUlcer: Boolean = false
)

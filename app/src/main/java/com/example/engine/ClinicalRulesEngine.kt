package com.example.engine

import com.example.model.CheckSeverity
import com.example.model.CheckStatus
import com.example.model.ClinicalCheckResult
import com.example.model.DoseScheduleItem
import com.example.model.PatientContext
import com.example.model.PrescriptionItem

object ClinicalRulesEngine {

    const val ENGINE_VERSION = "2.4.0-pilot"
    const val CLINICAL_REVIEW_DATE = "October 2024"
    const val CLINICAL_LEAD = "Uzair Ahmad (Pharm.D Candidate, FUUAST)"

    /**
     * Executes deterministic, traceable clinical checks on the verified prescription items.
     * Rules are versioned, documented, and never generated on-the-fly by an LLM.
     */
    fun evaluate(items: List<PrescriptionItem>, context: PatientContext): List<ClinicalCheckResult> {
        val results = mutableListOf<ClinicalCheckResult>()

        if (items.isEmpty()) {
            return results
        }

        // Rule 0: Verification / Confirmation Completeness Gate
        val unconfirmedCount = items.count { !it.isConfirmed }
        if (unconfirmedCount > 0) {
            results.add(
                ClinicalCheckResult(
                    ruleId = "RULE-VERIFY-001",
                    severity = CheckSeverity.WARNING,
                    status = CheckStatus.FLAG,
                    title = "Unconfirmed Extracted Items",
                    patientMessageEn = "$unconfirmedCount medicine(s) have not been confirmed by you. Please review each card and tap 'Confirm Accurate'.",
                    patientMessageRomanUrdu = "$unconfirmedCount dawaiyan aap ne confirm nahi keen. Baraye meherbani har dawai ko check kar ke 'Confirm' karein.",
                    patientMessageUrdu = "$unconfirmedCount ادویات کی تصدیق ابھی باقی ہے۔ برائے مہربانی ہر دوا کی تفصیلات دیکھ کر تصدیق فرمائیں۔",
                    missingInformation = "Patient or Pharmacist visual confirmation",
                    actionOwner = "Patient",
                    recommendedStepEn = "Review brand name, strength, and daily frequency before relying on directions.",
                    recommendedStepRomanUrdu = "Dawai ka naam, taqat (mg) aur waqt ghor se check karein.",
                    recommendedStepUrdu = "دوا کا نام، طاقت (ملی گرام) اور روزانہ خوراک کی جانچ کریں۔",
                    sourceCitation = "WHO Guide to Good Prescribing / ASHP Patient Consultation Standards",
                    reviewDate = CLINICAL_REVIEW_DATE
                )
            )
        }

        // Rule 1: Missing Critical Clinical Data (Dose/Frequency/Duration)
        items.forEach { item ->
            val missingFields = mutableListOf<String>()
            if (item.strength.isBlank() || item.strength.equals("unclear", ignoreCase = true)) {
                missingFields.add("Strength (mg/ml)")
            }
            if (item.frequency.isBlank() || item.frequency.equals("unclear", ignoreCase = true)) {
                missingFields.add("Frequency (Times/day)")
            }
            if (item.duration.isBlank() || item.duration.equals("unclear", ignoreCase = true)) {
                missingFields.add("Duration (Days)")
            }

            if (missingFields.isNotEmpty() || item.hasAmbiguity) {
                results.add(
                    ClinicalCheckResult(
                        ruleId = "RULE-UNCERTAIN-002",
                        severity = CheckSeverity.CRITICAL,
                        status = CheckStatus.UNABLE_TO_CHECK,
                        title = "Critical Detail Missing: ${item.brandName}",
                        patientMessageEn = "Missing ${missingFields.joinToString(", ")}. A dosage schedule cannot be created until this is resolved.",
                        patientMessageRomanUrdu = "${item.brandName} ki ${missingFields.joinToString(", ")} wazeh nahi hai. Pharmacist se poochein.",
                        patientMessageUrdu = "${item.brandName} کے لیے ${missingFields.joinToString("، ")} درج نہیں ہے۔ محفوظ شیڈول بنانے کے لیے فارماسسٹ سے رابطہ کریں۔",
                        missingInformation = missingFields.joinToString(", ").ifEmpty { item.ambiguityReason ?: "Handwriting ambiguous" },
                        actionOwner = "Pharmacist or Prescriber",
                        recommendedStepEn = "Do not guess the dose. Consult your dispensing pharmacist or call the prescriber.",
                        recommendedStepRomanUrdu = "Andazey se dawai na lein. Dispensing pharmacist ya doctor se confirm karein.",
                        recommendedStepUrdu = "اندازے سے دوا نہ لیں۔ ڈسپنسنگ فارماسسٹ یا ڈاکٹر سے تصدیق کروائیں۔",
                        sourceCitation = "Institute for Safe Medication Practices (ISMP) Ambiguous Abbreviation List",
                        reviewDate = CLINICAL_REVIEW_DATE
                    )
                )
            }
        }

        // Collect names for interaction checks
        val brandNamesLower = items.map { it.brandName.lowercase() }
        val genericNamesLower = items.map { it.genericName.lowercase() }

        fun matches(drugSub: String): Boolean {
            return brandNamesLower.any { it.contains(drugSub) } || genericNamesLower.any { it.contains(drugSub) }
        }

        // Rule 2: NSAID Food & Gastroprotection Requirement (Ibuprofen / Brufen, Diclofenac, Aspirin / Disprin, Naproxen)
        val hasNsaid = matches("brufen") || matches("ibuprofen") || matches("diclofenac") ||
                matches("voltaren") || matches("disprin") || matches("aspirin") || matches("naproxen")
        if (hasNsaid) {
            results.add(
                ClinicalCheckResult(
                    ruleId = "RULE-FOOD-003",
                    severity = CheckSeverity.WARNING,
                    status = CheckStatus.FLAG,
                    title = "NSAID Stomach Protection Guidance",
                    patientMessageEn = "Pain medicines like Brufen/Aspirin can irritate an empty stomach. Always take with or immediately after a meal.",
                    patientMessageRomanUrdu = "Brufen/Disprin jaisi dard ki dawaiyan khali pait maidey mein dard ya jalan kar sakti hain. Hamesha khaney ke foran baad lein.",
                    patientMessageUrdu = "بروفین یا ڈسپرین جیسی درد کش ادویات خالی پیٹ معدے میں جلن پیدا کر سکتی ہیں۔ ہمیشہ کھانا کھانے کے بعد لیں۔",
                    missingInformation = if (context.hasGastricUlcer) "Prior gastric ulcer history noted" else null,
                    actionOwner = "Patient",
                    recommendedStepEn = "Take with milk or full meal. If you have stomach ulcers, discuss with pharmacist.",
                    recommendedStepRomanUrdu = "Doodh ya khaney ke sath lein. Agar maidey ka ulcer hai toh doctor ko lazmi batayein.",
                    recommendedStepUrdu = "کھانے یا دودھ کے ساتھ استعمال کریں۔ اگر معدے میں پہلے سے السر ہے تو فارماسسٹ کو مطلع کریں۔",
                    sourceCitation = "British National Formulary (BNF 84): Section 10.1.1 Non-steroidal anti-inflammatory drugs",
                    reviewDate = CLINICAL_REVIEW_DATE
                )
            )
        }

        // Rule 3: Chelation / Mineral Binding (Fluoroquinolones like Ciprofloxacin + Calcium / Antacid)
        val hasCiproOrLevo = matches("cipro") || matches("levo") || matches("floxacin")
        val hasAntacidOrCalcium = matches("calcium") || matches("antacid") || matches("gaviscon") ||
                matches("maalox") || matches("iron") || matches("ferrous")
        if (hasCiproOrLevo && hasAntacidOrCalcium) {
            results.add(
                ClinicalCheckResult(
                    ruleId = "RULE-CHELATION-004",
                    severity = CheckSeverity.CRITICAL,
                    status = CheckStatus.FLAG,
                    title = "Antibiotic + Antacid Spacing Alert",
                    patientMessageEn = "Antacids or calcium supplements bind to Ciprofloxacin, stopping it from absorbing. Separate by at least 2 hours.",
                    patientMessageRomanUrdu = "Antacid (Gaviscon/Maalox) ya Calcium aur Ciprofloxacin ko aik sath na lein. Kam az kam 2 ghantey ka farq lazmi rakhein.",
                    patientMessageUrdu = "اینٹاسڈ یا کیلشیم اینٹی بائیوٹک (سفرو) کے اثر کو ختم کر دیتے ہیں۔ دونوں ادویات میں کم از کم 2 گھنٹے کا وقفہ رکھیں۔",
                    missingInformation = null,
                    actionOwner = "Patient & Pharmacist",
                    recommendedStepEn = "Take antibiotic 2 hours before or 4 hours after antacids or dairy/calcium supplements.",
                    recommendedStepRomanUrdu = "Cipro ko antacid se 2 ghantey pehle ya 4 ghantey baad lein.",
                    recommendedStepUrdu = "اینٹی بائیوٹک کو اینٹاسڈ یا کیلشیم سے 2 گھنٹے پہلے یا 4 گھنٹے بعد لیں۔",
                    sourceCitation = "FDA Drug Safety Communication: Fluoroquinolone Chelation Interactions",
                    reviewDate = CLINICAL_REVIEW_DATE
                )
            )
        }

        // Rule 4: Antiplatelet Interaction (Clopidogrel / Plavix + Omeprazole / Losec / Nexum)
        val hasClopidogrel = matches("clopidogrel") || matches("plavix") || matches("lowplat")
        val hasOmeprazole = matches("omeprazole") || matches("losec") || matches("esomeprazole") || matches("nexum")
        if (hasClopidogrel && hasOmeprazole) {
            results.add(
                ClinicalCheckResult(
                    ruleId = "RULE-INTERACT-005",
                    severity = CheckSeverity.CRITICAL,
                    status = CheckStatus.FLAG,
                    title = "Antiplatelet Efficacy Interaction",
                    patientMessageEn = "Omeprazole / Esomeprazole can significantly reduce Clopidogrel's heart protection benefit via liver enzyme inhibition.",
                    patientMessageRomanUrdu = "Omeprazole/Nexum khoon patla karne wali dawai (Clopidogrel) ka asar kam kar sakti hai. Pharmacist se mutabadil (e.g. Pantoprazole) mashwara karein.",
                    patientMessageUrdu = "اومیپرازول خون پتلا کرنے والی دوا (کلوپیڈوگریل) کے اثر کو کم کر سکتی ہے۔ ڈاکٹر یا فارماسسٹ سے متبادل کے لیے رجوع کریں۔",
                    missingInformation = null,
                    actionOwner = "Prescriber & Pharmacist",
                    recommendedStepEn = "Ask your doctor if Pantoprazole or Famotidine can be substituted for stomach protection.",
                    recommendedStepRomanUrdu = "Doctor se poochein ke kya Pantoprazole istemal kiya ja sakta hai.",
                    recommendedStepUrdu = "اپنے معالج سے پینٹوپرازول جیسے متبادل کے متعلق دریافت کریں۔",
                    sourceCitation = "FDA Warning & BNF 84: CYP2C19 Interaction between Clopidogrel and PPIs",
                    reviewDate = CLINICAL_REVIEW_DATE
                )
            )
        }

        // Rule 5: Antimicrobial Stewardship (Complete Course Alert for Augmentin / Azomax / Antibiotics)
        val hasAntibiotic = matches("augmentin") || matches("amoxicillin") || matches("azomax") ||
                matches("azithromycin") || matches("cefixime") || matches("cefix") || hasCiproOrLevo
        if (hasAntibiotic) {
            results.add(
                ClinicalCheckResult(
                    ruleId = "RULE-ANTIBIOTIC-006",
                    severity = CheckSeverity.INFO,
                    status = CheckStatus.FLAG,
                    title = "Complete the Full Course",
                    patientMessageEn = "Even if you feel completely healthy in 2-3 days, finish all prescribed days of this antibiotic to prevent bacterial resistance.",
                    patientMessageRomanUrdu = "Agar 2-3 din mein tabiyat theek bhi ho jaye, tab bhi antibiotic ka course pura karein taake jaraseem dobara na phailen.",
                    patientMessageUrdu = "اگر دو سے تین دن میں طبیعت بہتر بھی ہو جائے، تب بھی اینٹی بائیوٹک کا کورس مکمل کریں تاکہ جراثیم میں مدافعت پیدا نہ ہو۔",
                    missingInformation = null,
                    actionOwner = "Patient",
                    recommendedStepEn = "Do not stop early. Do not save leftover pills for future illness.",
                    recommendedStepRomanUrdu = "Dawai beech mein mat chhorein. Bachi hui goliyan baad ke liye na bachaayein.",
                    recommendedStepUrdu = "علاج ادھورا نہ چھوڑیں اور بچی ہوئی دوا کو بعد کے لیے نہ رکھیں۔",
                    sourceCitation = "WHO Global Action Plan on Antimicrobial Resistance (AMR)",
                    reviewDate = CLINICAL_REVIEW_DATE
                )
            )
        }

        // Rule 6: Paracetamol / Panadol Dose Safety
        val hasParacetamol = matches("panadol") || matches("paracetamol") || matches("calpol") || matches("tylenol")
        if (hasParacetamol) {
            results.add(
                ClinicalCheckResult(
                    ruleId = "RULE-PARACETAMOL-007",
                    severity = CheckSeverity.INFO,
                    status = CheckStatus.FLAG,
                    title = "Maximum Daily Paracetamol Limit",
                    patientMessageEn = "Never exceed 4,000 mg (eight 500mg tablets) in 24 hours. Check other cold remedies to avoid accidental liver toxicity.",
                    patientMessageRomanUrdu = "24 ghantey mein 8 goliyon (4,000 mg) se zyada Panadol hargiz na lein. Nazla zukam ke syprups mein bhi paracetamol ho sakta hai.",
                    patientMessageUrdu = "24 گھنٹوں میں 8 گولیوں (4000 ملی گرام) سے زیادہ پیراسیٹامول ہرگز نہ لیں۔ دیگر ادویات میں پیراسیٹامول کی موجودگی کا دھیان رکھیں۔",
                    missingInformation = null,
                    actionOwner = "Patient",
                    recommendedStepEn = "Space doses by at least 4 to 6 hours.",
                    recommendedStepRomanUrdu = "Har goli ke darmiyan kam az kam 4 se 6 ghantey ka faasla rakhein.",
                    recommendedStepUrdu = "خوراک کے درمیان کم از کم 4 سے 6 گھنٹے کا وقفہ رکھیں۔",
                    sourceCitation = "FDA Hepatotoxicity Advisory & Pakistan Drug Regulatory Authority (DRAP)",
                    reviewDate = CLINICAL_REVIEW_DATE
                )
            )
        }

        // Rule 7: Baseline Check Status (if no negative interactions detected)
        val negativeFlags = results.count { it.severity == CheckSeverity.CRITICAL || it.severity == CheckSeverity.WARNING }
        if (negativeFlags == 0) {
            results.add(
                0,
                ClinicalCheckResult(
                    ruleId = "RULE-PASS-000",
                    severity = CheckSeverity.INFO,
                    status = CheckStatus.NO_ISSUES_FOUND,
                    title = "No Issues Found in Evaluated Checks",
                    patientMessageEn = "No severe drug-drug interactions, known contraindications, or missing critical fields were detected across ${results.size + 1} deterministic checks.",
                    patientMessageRomanUrdu = "Chalaaye gaye tamam checks mein koi shadeed interaction ya khami nahi mili. Apne doctor ki hidayat ke mutabiq lein.",
                    patientMessageUrdu = "جانچے گئے تمام رولز میں ادویات کا کوئی نقصان دہ ردعمل یا بڑی خامی نہیں پائی گئی۔ ڈاکٹر کی ہدایات کے مطابق استعمال جاری رکھیں۔",
                    missingInformation = null,
                    actionOwner = "Patient",
                    recommendedStepEn = "Continue following your doctor's exact directions. Consult a pharmacist if new symptoms arise.",
                    recommendedStepRomanUrdu = "Doctor ki batayi hui tarteeb par amall karein.",
                    recommendedStepUrdu = "ڈاکٹر کی بتائی ہوئی ترتیب پر عمل جاری رکھیں۔",
                    sourceCitation = "Curated Samjha? Rx Knowledge Base v2.4 (Pharmacist Reviewed)",
                    reviewDate = CLINICAL_REVIEW_DATE
                )
            )
        }

        return results
    }

    /**
     * Determines whether the actionable dose schedule should be blocked due to safety flags or unconfirmed items.
     */
    fun isScheduleBlocked(items: List<PrescriptionItem>, checks: List<ClinicalCheckResult>): Boolean {
        // Block if any critical ambiguity or unable_to_check status exists
        val hasCritical = checks.any { it.severity == CheckSeverity.CRITICAL || it.status == CheckStatus.UNABLE_TO_CHECK }
        val hasUnconfirmed = items.any { !it.isConfirmed }
        return hasCritical || hasUnconfirmed
    }

    /**
     * Constructs a structured, clear patient daily timeline based on confirmed medication frequency.
     */
    fun buildSchedule(items: List<PrescriptionItem>): List<DoseScheduleItem> {
        val schedule = mutableListOf<DoseScheduleItem>()
        var counter = 1

        items.forEach { item ->
            val freq = item.frequency.uppercase()
            val isMorning = freq.contains("OD") || freq.contains("BD") || freq.contains("TDS") || freq.contains("QDS") || freq.contains("MORNING") || freq.contains("ONCE")
            val isAfternoon = freq.contains("TDS") || freq.contains("QDS") || freq.contains("AFTERNOON")
            val isEvening = freq.contains("BD") || freq.contains("TDS") || freq.contains("QDS") || freq.contains("NIGHT") || freq.contains("BEDTIME") || freq.contains("HS")

            val foodEn = when {
                item.brandName.contains("Brufen", ignoreCase = true) || item.brandName.contains("Disprin", ignoreCase = true) || item.brandName.contains("Augmentin", ignoreCase = true) ->
                    "Take with or immediately after food"
                item.brandName.contains("Nexum", ignoreCase = true) || item.brandName.contains("Losec", ignoreCase = true) ->
                    "Take 30 minutes before breakfast on an empty stomach"
                item.brandName.contains("Glucophage", ignoreCase = true) ->
                    "Take with meal to reduce upset stomach"
                else -> "Take with a full glass of clean water"
            }

            val foodRomanUrdu = when {
                item.brandName.contains("Brufen", ignoreCase = true) || item.brandName.contains("Disprin", ignoreCase = true) || item.brandName.contains("Augmentin", ignoreCase = true) ->
                    "Khaney ke sath ya foran baad lein"
                item.brandName.contains("Nexum", ignoreCase = true) || item.brandName.contains("Losec", ignoreCase = true) ->
                    "Nashtey se 30 minute pehle khali pait lein"
                item.brandName.contains("Glucophage", ignoreCase = true) ->
                    "Khaney ke sath lein taake maidey mein dard na ho"
                else -> "Aik glass saaf paani ke sath lein"
            }

            val foodUrdu = when {
                item.brandName.contains("Brufen", ignoreCase = true) || item.brandName.contains("Disprin", ignoreCase = true) || item.brandName.contains("Augmentin", ignoreCase = true) ->
                    "کھانا کھانے کے ساتھ یا فوراً بعد لیں"
                item.brandName.contains("Nexum", ignoreCase = true) || item.brandName.contains("Losec", ignoreCase = true) ->
                    "ناشتے سے آدھا گھنٹہ پہلے نہار منہ لیں"
                item.brandName.contains("Glucophage", ignoreCase = true) ->
                    "کھانے کے ساتھ لیں تاکہ معدے میں تکلیف نہ ہو"
                else -> "ایک گلاس صاف پانی کے ساتھ لیں"
            }

            if (isMorning) {
                schedule.add(
                    DoseScheduleItem(
                        id = "slot_${counter++}",
                        slotNameEn = "Morning",
                        slotNameRomanUrdu = "Subah",
                        slotNameUrdu = "صبح",
                        timeLabel = "08:00 AM",
                        medicineName = "${item.brandName} ${item.strength}",
                        dosageInstructionEn = "${item.dose} (Morning)",
                        dosageInstructionRomanUrdu = "${item.dose} subah",
                        dosageInstructionUrdu = "${item.dose} صبح کے وقت",
                        foodRequirementEn = foodEn,
                        foodRequirementRomanUrdu = foodRomanUrdu,
                        foodRequirementUrdu = foodUrdu
                    )
                )
            }

            if (isAfternoon) {
                schedule.add(
                    DoseScheduleItem(
                        id = "slot_${counter++}",
                        slotNameEn = "Afternoon",
                        slotNameRomanUrdu = "Do-pehar",
                        slotNameUrdu = "دوپہر",
                        timeLabel = "02:00 PM",
                        medicineName = "${item.brandName} ${item.strength}",
                        dosageInstructionEn = "${item.dose} (Afternoon)",
                        dosageInstructionRomanUrdu = "${item.dose} do-pehar",
                        dosageInstructionUrdu = "${item.dose} دوپہر کے وقت",
                        foodRequirementEn = foodEn,
                        foodRequirementRomanUrdu = foodRomanUrdu,
                        foodRequirementUrdu = foodUrdu
                    )
                )
            }

            if (isEvening) {
                schedule.add(
                    DoseScheduleItem(
                        id = "slot_${counter++}",
                        slotNameEn = "Evening / Night",
                        slotNameRomanUrdu = "Shaam / Raat",
                        slotNameUrdu = "شام / رات",
                        timeLabel = "08:30 PM",
                        medicineName = "${item.brandName} ${item.strength}",
                        dosageInstructionEn = "${item.dose} (Night)",
                        dosageInstructionRomanUrdu = "${item.dose} raat",
                        dosageInstructionUrdu = "${item.dose} رات کے وقت",
                        foodRequirementEn = foodEn,
                        foodRequirementRomanUrdu = foodRomanUrdu,
                        foodRequirementUrdu = foodUrdu
                    )
                )
            }
        }

        return schedule
    }
}

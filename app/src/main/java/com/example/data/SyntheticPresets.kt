package com.example.data

import com.example.model.PrescriptionItem

data class SyntheticScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val rawText: String,
    val items: List<PrescriptionItem>
)

object SyntheticPresets {

    val scenarios = listOf(
        SyntheticScenario(
            id = "scenario_infection_pain",
            title = "1. Augmentin 625mg + Brufen 400mg",
            subtitle = "Acute Respiratory Infection & Pain",
            description = "Demonstrates food timing, full antibiotic course compliance, and NSAID gastric safety.",
            rawText = """
                Rx
                1. Tab Augmentin 625mg - 1 tab BD x 5 days (After food)
                2. Tab Brufen 400mg - 1 tab TDS x 3 days (Strictly with food)
                3. Tab Panadol 500mg - 1 tab PRN for fever (Max 8 tabs/day)
            """.trimIndent(),
            items = listOf(
                PrescriptionItem(
                    id = "item_1",
                    brandName = "Augmentin",
                    genericName = "Amoxicillin + Clavulanic Acid",
                    strength = "625mg",
                    dose = "1 tablet",
                    frequency = "BD (Twice a day - morning & night)",
                    duration = "5 days",
                    specialInstructions = "Take at the start of a meal; complete full 5-day course",
                    confidence = 0.98f,
                    isConfirmed = true
                ),
                PrescriptionItem(
                    id = "item_2",
                    brandName = "Brufen",
                    genericName = "Ibuprofen",
                    strength = "400mg",
                    dose = "1 tablet",
                    frequency = "TDS (3 times a day)",
                    duration = "3 days",
                    specialInstructions = "Take with or immediately after a meal to protect stomach",
                    confidence = 0.96f,
                    isConfirmed = true
                ),
                PrescriptionItem(
                    id = "item_3",
                    brandName = "Panadol",
                    genericName = "Paracetamol",
                    strength = "500mg",
                    dose = "1-2 tablets",
                    frequency = "PRN (As needed for pain/fever)",
                    duration = "As needed",
                    specialInstructions = "Never exceed 4,000mg (8 tablets) in 24 hours",
                    confidence = 0.99f,
                    isConfirmed = true
                )
            )
        ),
        SyntheticScenario(
            id = "scenario_cardiometabolic",
            title = "2. Cardiometabolic Chronic Regimen",
            subtitle = "Metformin + Aspirin + Atorvastatin + PPI",
            description = "Demonstrates daily chronic schedule, evening statin timing, and breakfast PPI.",
            rawText = """
                Rx
                1. Tab Glucophage 500mg - 1 tab BD with meals (Morning & Evening)
                2. Tab Disprin 75mg - 1 tab OD after breakfast
                3. Tab Lipitor 20mg - 1 tab OD at night (Bedtime)
                4. Cap Nexum 20mg - 1 cap OD 30 mins before breakfast
            """.trimIndent(),
            items = listOf(
                PrescriptionItem(
                    id = "item_4",
                    brandName = "Glucophage",
                    genericName = "Metformin Hydrochloride",
                    strength = "500mg",
                    dose = "1 tablet",
                    frequency = "BD (Twice a day with meals)",
                    duration = "Chronic / Monthly refill",
                    specialInstructions = "Take with food to minimize nausea or stomach upset",
                    confidence = 0.97f,
                    isConfirmed = true
                ),
                PrescriptionItem(
                    id = "item_5",
                    brandName = "Disprin",
                    genericName = "Aspirin (Low-dose antiplatelet)",
                    strength = "75mg",
                    dose = "1 tablet",
                    frequency = "OD (Once daily after breakfast)",
                    duration = "Chronic / Long-term",
                    specialInstructions = "Take with full glass of water after breakfast",
                    confidence = 0.95f,
                    isConfirmed = true
                ),
                PrescriptionItem(
                    id = "item_6",
                    brandName = "Lipitor",
                    genericName = "Atorvastatin Calcium",
                    strength = "20mg",
                    dose = "1 tablet",
                    frequency = "OD (Once daily at night)",
                    duration = "Chronic / Long-term",
                    specialInstructions = "Best taken at bedtime; avoid large amounts of grapefruit",
                    confidence = 0.98f,
                    isConfirmed = true
                ),
                PrescriptionItem(
                    id = "item_7",
                    brandName = "Nexum",
                    genericName = "Esomeprazole Magnesium",
                    strength = "20mg",
                    dose = "1 capsule",
                    frequency = "OD (Once daily morning)",
                    duration = "14 days",
                    specialInstructions = "Take on an empty stomach 30-60 mins before breakfast",
                    confidence = 0.96f,
                    isConfirmed = true
                )
            )
        ),
        SyntheticScenario(
            id = "scenario_chelation_alert",
            title = "3. Ciprofloxacin + Antacid (Spacing Alert)",
            subtitle = "Antibiotic & Chelation Warning",
            description = "Demonstrates critical interaction detection: mineral antacid binds antibiotic, requiring 2-hour separation.",
            rawText = """
                Rx
                1. Tab Ciproxin 500mg - 1 tab BD x 7 days
                2. Susp Gaviscon / Antacid - 2 spoonfuls TDS for heartburn
            """.trimIndent(),
            items = listOf(
                PrescriptionItem(
                    id = "item_8",
                    brandName = "Ciproxin",
                    genericName = "Ciprofloxacin",
                    strength = "500mg",
                    dose = "1 tablet",
                    frequency = "BD (Every 12 hours)",
                    duration = "7 days",
                    specialInstructions = "Drink plenty of fluids; avoid direct sunlight exposure",
                    confidence = 0.97f,
                    isConfirmed = true
                ),
                PrescriptionItem(
                    id = "item_9",
                    brandName = "Gaviscon / Antacid",
                    genericName = "Aluminum/Magnesium Hydroxide + Calcium",
                    strength = "Standard Oral Suspension",
                    dose = "2 spoonfuls (10ml)",
                    frequency = "TDS (3 times daily after meals)",
                    duration = "As needed",
                    specialInstructions = "Must be separated from Cipro by at least 2 hours",
                    confidence = 0.94f,
                    isConfirmed = true
                )
            )
        ),
        SyntheticScenario(
            id = "scenario_ambiguous_rx",
            title = "4. Ambiguous Handwritten Rx (Uncertainty Gate)",
            subtitle = "Missing Strength & Dose Ambiguity",
            description = "Demonstrates safety gate: unresolved items stay unverified and block actionable dose schedule until confirmed.",
            rawText = """
                Rx (Handwritten note):
                1. Augmentin ... BD (strength smudged)
                2. Painkiller tab ... (frequency illegible)
            """.trimIndent(),
            items = listOf(
                PrescriptionItem(
                    id = "item_10",
                    brandName = "Augmentin",
                    genericName = "Amoxicillin + Clavulanic Acid",
                    strength = "Unclear (smudged 375mg vs 625mg vs 1g)",
                    dose = "1 tablet",
                    frequency = "BD (Twice a day)",
                    duration = "Unclear",
                    specialInstructions = "Missing exact strength",
                    confidence = 0.45f,
                    isConfirmed = false,
                    hasAmbiguity = true,
                    ambiguityReason = "Strength value is unreadable or missing. Dose cannot be inferred safely.",
                    unresolvedFields = listOf("Strength (mg)", "Duration")
                ),
                PrescriptionItem(
                    id = "item_11",
                    brandName = "Analgesic / Pain Tab",
                    genericName = "Unidentified NSAID",
                    strength = "Unclear",
                    dose = "1 tablet",
                    frequency = "Unclear (abbreviation illegible)",
                    duration = "Unclear",
                    specialInstructions = "Illegible handwriting",
                    confidence = 0.30f,
                    isConfirmed = false,
                    hasAmbiguity = true,
                    ambiguityReason = "Drug name and dosing frequency are illegible. Clinical check halted.",
                    unresolvedFields = listOf("Drug Name", "Strength", "Frequency", "Duration")
                )
            )
        )
    )
}

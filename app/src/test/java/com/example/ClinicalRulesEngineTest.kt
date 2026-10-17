package com.example

import com.example.engine.ClinicalRulesEngine
import com.example.model.CheckSeverity
import com.example.model.CheckStatus
import com.example.model.PatientContext
import com.example.model.PrescriptionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalRulesEngineTest {

    @Test
    fun testNsaidGastricProtectionRuleEvaluated() {
        val items = listOf(
            PrescriptionItem(
                id = "1",
                brandName = "Brufen",
                genericName = "Ibuprofen",
                strength = "400mg",
                dose = "1 tablet",
                frequency = "TDS",
                duration = "3 days",
                isConfirmed = true
            )
        )

        val checks = ClinicalRulesEngine.evaluate(items, PatientContext())
        val foodRule = checks.find { it.ruleId == "RULE-FOOD-003" }

        assertTrue("RULE-FOOD-003 should be evaluated for NSAIDs", foodRule != null)
        assertEquals(CheckSeverity.WARNING, foodRule?.severity)
        assertTrue(foodRule?.patientMessageEn?.contains("Brufen") == true)
        assertTrue(foodRule?.patientMessageRomanUrdu?.contains("khali pait") == true)
    }

    @Test
    fun testChelationInteractionBetweenCiprofloxacinAndAntacid() {
        val items = listOf(
            PrescriptionItem(
                id = "1",
                brandName = "Ciproxin",
                genericName = "Ciprofloxacin",
                strength = "500mg",
                dose = "1 tablet",
                frequency = "BD",
                duration = "7 days",
                isConfirmed = true
            ),
            PrescriptionItem(
                id = "2",
                brandName = "Gaviscon / Antacid",
                genericName = "Calcium Carbonate + Aluminum Hydroxide",
                strength = "10ml",
                dose = "2 spoonfuls",
                frequency = "TDS",
                duration = "As needed",
                isConfirmed = true
            )
        )

        val checks = ClinicalRulesEngine.evaluate(items, PatientContext())
        val chelationRule = checks.find { it.ruleId == "RULE-CHELATION-004" }

        assertTrue("RULE-CHELATION-004 must flag Cipro + Antacid interaction", chelationRule != null)
        assertEquals(CheckSeverity.CRITICAL, chelationRule?.severity)
        assertEquals(CheckStatus.FLAG, chelationRule?.status)
    }

    @Test
    fun testUnconfirmedOrAmbiguousItemBlocksSchedule() {
        val ambiguousItems = listOf(
            PrescriptionItem(
                id = "1",
                brandName = "Augmentin",
                genericName = "Amoxicillin + Clavulanic Acid",
                strength = "Unclear",
                dose = "1 tablet",
                frequency = "Unclear",
                duration = "5 days",
                isConfirmed = false,
                hasAmbiguity = true
            )
        )

        val checks = ClinicalRulesEngine.evaluate(ambiguousItems, PatientContext())
        val isBlocked = ClinicalRulesEngine.isScheduleBlocked(ambiguousItems, checks)

        assertTrue("Actionable dose schedule must be blocked when items are ambiguous or unconfirmed", isBlocked)
        val uncertaintyCheck = checks.find { it.ruleId == "RULE-UNCERTAIN-002" }
        assertEquals(CheckStatus.UNABLE_TO_CHECK, uncertaintyCheck?.status)
    }

    @Test
    fun testConfirmedItemsUnblockSchedule() {
        val confirmedItems = listOf(
            PrescriptionItem(
                id = "1",
                brandName = "Augmentin",
                genericName = "Amoxicillin + Clavulanic Acid",
                strength = "625mg",
                dose = "1 tablet",
                frequency = "BD",
                duration = "5 days",
                isConfirmed = true
            )
        )

        val checks = ClinicalRulesEngine.evaluate(confirmedItems, PatientContext())
        val isBlocked = ClinicalRulesEngine.isScheduleBlocked(confirmedItems, checks)

        assertFalse("Schedule should not be blocked when all items are confirmed with clear parameters", isBlocked)

        val schedule = ClinicalRulesEngine.buildSchedule(confirmedItems)
        assertTrue("Schedule slots should be created for BD frequency (Morning & Night)", schedule.size >= 2)
    }
}

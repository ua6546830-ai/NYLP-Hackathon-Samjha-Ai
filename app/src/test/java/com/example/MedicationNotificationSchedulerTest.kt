package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DoseScheduleItem
import com.example.notification.MedicationNotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MedicationNotificationSchedulerTest {

    @Test
    fun testScheduleAndCancelReminders() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val slots = listOf(
            DoseScheduleItem(
                id = "slot_1",
                slotNameEn = "Morning",
                slotNameRomanUrdu = "Subah",
                slotNameUrdu = "صبح",
                timeLabel = "08:00 AM",
                medicineName = "Augmentin 625mg",
                dosageInstructionEn = "1 tablet (Morning)",
                dosageInstructionRomanUrdu = "1 tablet subah",
                dosageInstructionUrdu = "1 گولی صبح",
                foodRequirementEn = "Take after meals",
                foodRequirementRomanUrdu = "Khaney ke baad",
                foodRequirementUrdu = "کھانے کے بعد"
            ),
            DoseScheduleItem(
                id = "slot_2",
                slotNameEn = "Night",
                slotNameRomanUrdu = "Raat",
                slotNameUrdu = "رات",
                timeLabel = "08:30 PM",
                medicineName = "Augmentin 625mg",
                dosageInstructionEn = "1 tablet (Night)",
                dosageInstructionRomanUrdu = "1 tablet raat",
                dosageInstructionUrdu = "1 گولی رات",
                foodRequirementEn = "Take after meals",
                foodRequirementRomanUrdu = "Khaney ke baad",
                foodRequirementUrdu = "کھانے کے بعد"
            )
        )

        val scheduled = MedicationNotificationScheduler.scheduleAllReminders(context, slots)
        assertTrue("Reminders should be successfully registered", scheduled)
        assertTrue("Reminders state should be active in scheduler", MedicationNotificationScheduler.areRemindersActive(context))
        assertEquals(2, MedicationNotificationScheduler.getScheduledCount(context))

        MedicationNotificationScheduler.cancelAllReminders(context, slots.size)
        assertFalse("Reminders should be inactive after cancellation", MedicationNotificationScheduler.areRemindersActive(context))
        assertEquals(0, MedicationNotificationScheduler.getScheduledCount(context))
    }
}

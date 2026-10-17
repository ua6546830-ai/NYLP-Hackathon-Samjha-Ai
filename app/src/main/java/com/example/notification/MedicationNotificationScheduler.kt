package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.model.DoseScheduleItem
import java.util.Calendar

object MedicationNotificationScheduler {

    private const val PREFS_NAME = "samjha_reminders_prefs"
    private const val KEY_REMINDERS_ACTIVE = "reminders_active"
    private const val KEY_SCHEDULED_COUNT = "scheduled_count"

    fun areRemindersActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMINDERS_ACTIVE, false)
    }

    fun getScheduledCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SCHEDULED_COUNT, 0)
    }

    /**
     * Schedules daily alarm reminders for all confirmed dose slots extracted from the prescription.
     */
    fun scheduleAllReminders(context: Context, slots: List<DoseScheduleItem>): Boolean {
        if (slots.isEmpty()) return false

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        slots.forEachIndexed { index, slot ->
            val (hour, minute) = parseTimeLabel(slot.timeLabel)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // If time has already passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val requestCode = 1000 + index
            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra(MedicationReminderReceiver.EXTRA_MED_NAME, slot.medicineName)
                putExtra(MedicationReminderReceiver.EXTRA_SLOT_NAME, slot.slotNameEn)
                putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, slot.dosageInstructionEn)
                putExtra(MedicationReminderReceiver.EXTRA_FOOD_NOTE, slot.foodRequirementEn)
                putExtra(MedicationReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // If exact alarm permission is restricted, fallback to inexact
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REMINDERS_ACTIVE, true)
            .putInt(KEY_SCHEDULED_COUNT, slots.size)
            .apply()

        return true
    }

    /**
     * Sends an immediate test notification (or fires after a 3-second delay) so the user can verify notifications.
     */
    fun sendImmediateTestNotification(context: Context, slot: DoseScheduleItem? = null) {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(
                MedicationReminderReceiver.EXTRA_MED_NAME,
                slot?.medicineName ?: "Augmentin 625mg"
            )
            putExtra(
                MedicationReminderReceiver.EXTRA_SLOT_NAME,
                slot?.slotNameEn ?: "Morning Dose"
            )
            putExtra(
                MedicationReminderReceiver.EXTRA_DOSAGE,
                slot?.dosageInstructionEn ?: "1 tablet (Morning)"
            )
            putExtra(
                MedicationReminderReceiver.EXTRA_FOOD_NOTE,
                slot?.foodRequirementEn ?: "Take with or immediately after food"
            )
            putExtra(MedicationReminderReceiver.EXTRA_NOTIFICATION_ID, 9999)
        }
        context.sendBroadcast(intent)
    }

    /**
     * Cancels all scheduled reminder alarms.
     */
    fun cancelAllReminders(context: Context, slotsCount: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until maxOf(slotsCount, 10)) {
            val requestCode = 1000 + i
            val intent = Intent(context, MedicationReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REMINDERS_ACTIVE, false)
            .putInt(KEY_SCHEDULED_COUNT, 0)
            .apply()
    }

    private fun parseTimeLabel(timeLabel: String): Pair<Int, Int> {
        return try {
            val isPM = timeLabel.contains("PM", ignoreCase = true)
            val isAM = timeLabel.contains("AM", ignoreCase = true)
            val clean = timeLabel.replace("AM", "", ignoreCase = true)
                .replace("PM", "", ignoreCase = true)
                .trim()
            val parts = clean.split(":")
            var hour = parts[0].trim().toInt()
            val minute = if (parts.size > 1) parts[1].trim().toInt() else 0

            if (isPM && hour < 12) hour += 12
            if (isAM && hour == 12) hour = 0
            Pair(hour, minute)
        } catch (e: Exception) {
            Pair(8, 0) // Default 8:00 AM
        }
    }
}
